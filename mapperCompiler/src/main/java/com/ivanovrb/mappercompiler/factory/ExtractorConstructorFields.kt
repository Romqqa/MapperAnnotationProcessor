package com.ivanovrb.mappercompiler.factory

import com.ivanovrb.mapper.Default
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.MappingName
import com.ivanovrb.mappercompiler.MapperUtils
import com.ivanovrb.mappercompiler.getStubCollection
import com.ivanovrb.mappercompiler.isPrimitive
import com.ivanovrb.mappercompiler.resolver.ConstructorConflictResolver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

abstract class ExtractorConstructorFields(
        private val graphDependencies: Map<String, String>,
        private val processingEnv: ProcessingEnvironment) {

    abstract val primaryConstructor: Constructor

    private val targetConstructor: Constructor by lazy {
        val constructors = getConstructorsFromElement(targetElement)
        if (constructors.size == 1)
            constructors.first()
        else
            resolveConstructorsConflict(constructors)
    }

    private fun getTargetTypeOfVariable(variable: Parameter): TypeName? {
        return targetConstructor.parameters.findLast {
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
            if (parameter.getAnnotation(IgnoreMap::class.java) != null) {
                return@forEach
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

        if (typeVariable.isPrimitive() && variable.getAnnotation(Nullable::class.java) != null && targetType?.nullable == false) {
            val defValueAnnotation = variable.getAnnotation(Default::class.java)

            val defValue = if (defValueAnnotation == null) {
                MapperUtils.getDefValue(typeVariable.asNonNullable().toString()).toString()
            } else {
                if (typeVariable.toString() == String::class.java.canonicalName)
                    "\"${defValueAnnotation.value}\""
                else
                    defValueAnnotation.value
            }

            resultMap[variable.simpleName] = resultMap[variable.simpleName]!!.first to "${resultMap[variable.simpleName]!!.second} ?: $defValue"
        }

        return resultMap
    }

    private fun generateStatementForUnknownTypeClass(variable: Parameter): String? {
        if (variable.typeName is ParameterizedTypeName) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, variable.typeName.typeArguments.map { it.nullable }.toString())
        }
        return "${variable.simpleName} ?: ${ variable.typeName.getStubCollection()?: variable.typeName.asNonNullable()}()"
    }

    private fun generateStatementForNotPrimaryField(variable: Parameter, mapperType: String): String? {
        return if (variable.getAnnotation(Nullable::class.java) == null) {
            "${variable.simpleName}.mapTo${ClassName.bestGuess(mapperType).simpleName}()"
        } else {
            val defaultAnnotation = variable.getAnnotation(Default::class.java)
            "(${variable.simpleName} ?: ${defaultAnnotation?.value
                    ?: variable.typeName.asNonNullable().toString()}()).mapTo${ClassName.bestGuess(mapperType).simpleName}()"
        }
    }

    private fun getMappingNameOrDefault(variable: Parameter): Pair<String, String?> {
        val mappingNameAnnotation = variable.getAnnotation(MappingName::class.java)
        return if (mappingNameAnnotation != null) {
            mappingNameAnnotation.value to variable.simpleName
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
                                    .filter { it.getAnnotation(IgnoreMap::class.java) == null }
                                    .map {
                                        val isNullable = it.getAnnotation(Nullable::class.java) != null
                                        Parameter(it.simpleName.toString(), if (isNullable) it.asType().asTypeName().asNullable() else it.asType().asTypeName(), it.annotationMirrors)
                                    }
                                    .toList(),
                            executableElement.annotationMirrors)
                }
                .toList()
    }

    protected fun resolveConstructorsConflict(constructors: List<Constructor>): Constructor {
        return ConstructorConflictResolver(processingEnv, primaryElement).resolve(constructors, getConstructorsFromElement(targetElement))
    }
}

