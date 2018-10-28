package com.ivanovrb.mappercompiler.factory

import com.ivanovrb.mapper.Default
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.MappingName
import com.ivanovrb.mappercompiler.MapperUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

abstract class ExtractorConstructorFields(
        private val graphDependencies: Map<String, String>,
        private val processingEnv: ProcessingEnvironment) {

    abstract val primaryConstructor: Constructor
    val targetConstructorParameter: Map<String, TypeName>
        get() = getConstructorFromElement(targetElement).parameters.map { it.simpleName.toString() to it.asType().asTypeName() }.toMap()

    abstract fun getTargetTypeOfVariable(variable: Parameter): TypeName?

    abstract fun getConstructorParametersFromElement(element: Element): Map<String, TypeName>

    protected lateinit var primaryElement: Element
    protected lateinit var targetElement: Element

    fun extract(primaryElement: Element, targetElement: Element): Map<String, Pair<String, String?>> {
        val resultMap = hashMapOf<String, Pair<String, String?>>()

        this.primaryElement = primaryElement
        this.targetElement = targetElement

        primaryConstructor.parameters.forEach { parameter ->
            if (parameter.getAnnotation(IgnoreMap::class.java) != null) {
                return@forEach
            }

            resultMap.putAll(extractValues(parameter, targetElement))
        }
        return resultMap
    }

    private fun extractValues(variable: Parameter, targetElement: Element): Map<out String, Pair<String, String?>> {
        val resultMap = hashMapOf<String, Pair<String, String?>>()
        resultMap[variable.simpleName.toString()] = getMappingNameOrDefault(variable)

        val typeVariable = variable.typeName

        val targetType = getTargetTypeOfVariable(variable)

        val isSameType = typeVariable.toString() == targetType.toString()

        if (!MapperUtils.isPrimitive(typeVariable.toString()) && !isSameType) {
//            val typeVariableAsElement = (typeVariable as DeclaredType).asElement()
            val mapperType = graphDependencies[typeVariable.toString()]

            if (mapperType == null) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Class $typeVariable has not mapper method ")
            } else {
                val statement = if (variable.getAnnotation(Nullable::class.java) == null) {
                    "${variable.simpleName}.mapTo${ClassName.bestGuess(mapperType).simpleName}()"
                } else {
                    val defaultAnnotation = variable.getAnnotation(Default::class.java)
                    "(${variable.simpleName} ?: ${defaultAnnotation?.value
                            ?: typeVariable.toString() +"()"}).mapTo${ClassName.bestGuess(mapperType).simpleName}()"

                }
                resultMap[variable.simpleName.toString()] = resultMap[variable.simpleName.toString()]!!.first to statement
            }
        }

        if (MapperUtils.isPrimitive(typeVariable.toString()) && variable.getAnnotation(Nullable::class.java) != null) {
            val defValueAnnotation = variable.getAnnotation(Default::class.java)

            val defValue = if (defValueAnnotation == null) {
                MapperUtils.getDefValue(typeVariable.toString()).toString()
            } else {
                if (typeVariable.toString() == String::class.java.canonicalName)
                    "\"${defValueAnnotation.value}\""
                else
                    defValueAnnotation.value
            }

            resultMap[variable.simpleName.toString()] = resultMap[variable.simpleName.toString()]!!.first to "${resultMap[variable.simpleName.toString()]!!.second} ?: $defValue"
        }

        return resultMap
    }

    private fun getMappingNameOrDefault(variable: Parameter): Pair<String, String?> {
        val mappingNameAnnotation = variable.getAnnotation(MappingName::class.java)
        return if (mappingNameAnnotation != null) {
            mappingNameAnnotation.value to variable.simpleName.toString()
        } else {
            variable.simpleName.toString() to variable.simpleName.toString()
        }
    }


    protected fun getConstructorFromElement(element: Element): ExecutableElement {
        return ElementFilter.constructorsIn(element.enclosedElements)
                .first()
    }
}

