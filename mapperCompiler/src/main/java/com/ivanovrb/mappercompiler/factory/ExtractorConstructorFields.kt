package com.ivanovrb.mappercompiler.factory

import com.ivanovrb.mappercompiler.*
import com.ivanovrb.mappercompiler.resolver.ConstructorConflictResolver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

abstract class ExtractorConstructorFields(
        private val elementsUtils:Elements,
        private val graphDependencies: Map<String, String>,
        private val processingEnv: ProcessingEnvironment) {

    abstract val primaryConstructor: Constructor

    private val targetConstructor: Constructor by lazy {
        val constructors = getConstructorsFromElement(targetElement)

        if (constructors.size == 1)
            constructors.first()
        else {
            resolveConstructorsConflict(constructors, targetElement, primaryElement)
        }
    }

    private fun getTargetTypeOfVariable(variable: Parameter): TypeName? {
        return targetConstructor.parameters.firstOrNull {
            it.argName == variable.simpleName ||
                    it.simpleName == variable.simpleName ||
                    it.simpleName == variable.argName ||
                    it.argName == variable.argName
        }?.typeName
    }

    protected lateinit var primaryElement: Element
    private lateinit var targetElement: Element

    fun extract(primaryElement: Element, targetElement: Element): Map<String, Pair<String, String?>> {
        val resultMap = hashMapOf<String, Pair<String, String?>>()

        this.primaryElement = primaryElement
        this.targetElement = targetElement

        primaryConstructor.parameters.forEach { parameter ->
            parameter.annotationMirrors.forEachIndexed { _, annotationMirror ->
                if (annotationMirror.annotationType.toString() == IGNORE_MAP_CLASS_NAME){
                    return@forEach
                }
            }

            resultMap.putAll(extractValues(parameter))
        }
        return resultMap
    }

    private fun extractValues(variable: Parameter): Map<out String, Pair<String, String?>> {
        val resultMap = hashMapOf<String, Pair<String, String?>>()
        resultMap[variable.simpleName] = getMappingNameOrDefault(variable)

        val typeVariable = variable.typeName
        val targetType = getTargetTypeOfVariable(variable)
        val isSameType = typeVariable.asNonNullable().toString() == targetType?.asNonNullable().toString()

        if (!typeVariable.isPrimitive()) {
            if (!isSameType) {
                val mapperType = graphDependencies[typeVariable.asNonNullable().toString()]

                if (mapperType == null) {
                    val statement = generateStatementForUnknownTypeClass(variable)
                    resultMap[variable.simpleName] = resultMap[variable.simpleName]!!.first to statement
                } else {
                    resultMap[variable.simpleName] = resultMap[variable.simpleName]!!.first to generateStatementForNotPrimaryField(variable, mapperType)
                }
            } else {
                if (typeVariable.nullable && targetType?.nullable == false) {
                    val statement = generateStatementForUnknownTypeClass(variable)
                    resultMap[variable.simpleName] = resultMap[variable.simpleName]!!.first to statement
                }
            }
        }

        if (typeVariable.isPrimitive() && variable.annotationMirrors.firstOrNull { Nullable::class.qualifiedName == it.annotationType.toString() } != null && targetType?.nullable == false) {

            var defValueAnnotation:AnnotationMirror? = null
            variable.annotationMirrors.forEachIndexed { index, annotationMirror ->
                if (annotationMirror.annotationType.toString() == DEFAULT_CLASS_NAME){
                    defValueAnnotation = annotationMirror
                    return@forEachIndexed
                }
            }

            val defValue = if (defValueAnnotation == null) {
                MapperUtils.getDefValue(typeVariable.asNonNullable().toString()).toString()
            } else {
                if (typeVariable.toString() == String::class.java.canonicalName)
                    "\"${defValueAnnotation?.elementValues?.filterKeys { "value" == it.simpleName.toString() }?.mapValues { it.value.value as String }}\""
                else
                    defValueAnnotation?.elementValues?.filterKeys { "value" == it.simpleName.toString() }?.mapValues { it.value.value as String }?.toList()?.firstOrNull()?.second
            }

            resultMap[variable.simpleName] = resultMap[variable.simpleName]!!.first to "${resultMap[variable.simpleName]!!.second} ?: $defValue"
        }

        return resultMap
    }

    private fun generateStatementForUnknownTypeClass(variable: Parameter): String? {
        return "${variable.simpleName} ?: ${ variable.typeName.getStubCollection()?: variable.typeName.asNonNullable()}()"
    }

    private fun generateStatementForNotPrimaryField(variable: Parameter, mapperType: String): String? {
        return if (variable.annotationMirrors.firstOrNull { Nullable::class.qualifiedName == it.annotationType.toString() } == null) {
            "${variable.simpleName}.mapTo${ClassName.bestGuess(mapperType).simpleName}()"
        } else {
            val defaultAnnotation: AnnotationMirror? = variable.annotationMirrors.firstOrNull { DEFAULT_CLASS_NAME == it.annotationType.toString() }

            "(${variable.simpleName} ?: ${defaultAnnotation?.elementValues?.filterKeys { "value" == it.simpleName.toString() }?.mapValues { it.value.value as kotlin.String }?.toList()?.firstOrNull()?.second
                    ?: variable.typeName.asNonNullable().toString()}()).mapTo${ClassName.bestGuess(mapperType).simpleName}()"
        }
    }

    private fun getMappingNameOrDefault(variable: Parameter): Pair<String, String?> {
        val mappingNameAnnotation: AnnotationMirror? = variable.annotationMirrors.firstOrNull { MAPPING_NAME_CLASS_NAME == it.annotationType.toString() }
        return if (mappingNameAnnotation != null) {
            mappingNameAnnotation.elementValues?.filterKeys { "value" == it.simpleName.toString() }?.mapValues { it.value.value as kotlin.String }?.toList()?.firstOrNull()?.second.toString() to variable.simpleName
        } else {
            variable.simpleName to variable.simpleName
        }
    }


    protected fun getConstructorsFromElement(element: Element): List<Constructor> {
        return ElementFilter.constructorsIn(element.enclosedElements)
                .asSequence()
                .filter { it.parameters.isNotEmpty() }
                .filter { it.parameters[0].asType().toString() != "android.os.Parcel" }
                .sortedBy { it.parameters.size }
                .sortedBy { it.simpleName.toString() }
                .map { executableElement ->
                    Constructor(
                            executableElement.parameters
                                    .asSequence()
                                    .filter {
                                        it.annotationMirrors.firstOrNull { IGNORE_MAP_CLASS_NAME == it.annotationType.toString()} == null
                                    }
                                    .map {
                                        val isNullable = it.annotationMirrors.firstOrNull { Nullable::class.qualifiedName == it.annotationType.toString() } != null
                                        Parameter(it.simpleName.toString(), if (isNullable) it.asType().asTypeName().asNullable() else it.asType().asTypeName(), it.annotationMirrors)
                                    }
                                    .toList(),
                            executableElement.annotationMirrors)
                }
                .toList()
    }

    protected fun resolveConstructorsConflict(constructors: List<Constructor>, firstElement: Element = primaryElement, secondElement: Element = targetElement): Constructor {
//        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, primaryElement.simpleName.toString().append { targetElement.simpleName })
        return ConstructorConflictResolver(processingEnv, firstElement).resolve(constructors, getConstructorsFromElement(secondElement))
    }
}

