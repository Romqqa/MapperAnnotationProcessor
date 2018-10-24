package com.ivanovrb.mappercompiler

import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import com.squareup.kotlinpoet.*
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.ElementFilter

class MapperProcessor : AbstractProcessor() {

    companion object {
        val KAPT_KOTLIN_GENERATED_OPTION = "kapt.kotlin.generated"
    }

    private val primaryData = hashMapOf<Element, Element>()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Mapper::class.java.canonicalName, IgnoreMap::class.java.canonicalName, MappingName::class.java.canonicalName)

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        val annotatedClasses = roundEnvironment?.getElementsAnnotatedWith(Mapper::class.java)
        annotatedClasses?.forEach { element ->
            if (element.kind != ElementKind.CLASS) throw Throwable("Mapper annotation apply only class")

            val targetClass = element.annotationMirrors.firstOrNull()?.elementValues?.entries?.first()?.value?.value as DeclaredType

            val constructorParameters = mutableListOf<VariableElement>()
            constructorParameters.addAll(ElementFilter.constructorsIn(element.enclosedElements).first().parameters)

            primaryData[element] = targetClass.asElement()
        }

        generateClass()
        return true
    }

    private fun generateClass() {
        val file = FileSpec.builder("", "Mapper")
                .addType(buildClass())
                .build()

        val options = processingEnv.options
        val kotlinGenerated = options[KAPT_KOTLIN_GENERATED_OPTION]
        val mapperFile = File(kotlinGenerated, "mapper")

        mapperFile.mkdir()

        file.writeTo(mapperFile)
    }

    private fun buildClass(): TypeSpec {
        val mapperClassBuilder = TypeSpec.objectBuilder("Mapper")

        primaryData.forEach {
            mapperClassBuilder
                    .addFunction(buildFunctions(it.key, it.value))
            mapperClassBuilder
                    .addFunction(buildFunctions(it.value, it.key))
        }

        return mapperClassBuilder.build()
    }

    private fun buildFunctions(primaryElement: Element, targetElement: Element): FunSpec {
        val primaryFieldsMap = getConstructorFields(primaryElement)
        val targetFieldsMap = getConstructorFields(targetElement)

        if (primaryFieldsMap.values.toString() != targetFieldsMap.values.toString()) throw IllegalArgumentException("${primaryElement.simpleName} fields (${primaryFieldsMap.values}) and ${targetElement.simpleName} fields (${targetFieldsMap.values}) are different")

        val parametersStringBuilder = StringBuilder()

        val primaryFieldsNamesList = primaryFieldsMap.keys.toList()
        val targetFieldsNamesList = targetFieldsMap.keys.toList()
        targetFieldsNamesList.forEachIndexed { index, s ->

            parametersStringBuilder
                    .append("\t")
                    .append(s)
                    .append(" = ")
                    .append(primaryFieldsNamesList[index])
                    .append(",\n")
        }

        return FunSpec.builder("map${primaryElement.simpleName}To${targetElement.simpleName}")
                .addParameter(primaryElement.simpleName.toString().decapitalize(), primaryElement.asType().asTypeName())
                .returns(targetElement.asType().asTypeName())
                .addStatement("with(${primaryElement.simpleName.toString().decapitalize()}){\nreturn ${targetElement.simpleName}(\n${parametersStringBuilder.dropLast(2)}\n)}")
                .build()

    }

    private fun getConstructorFields(element: Element): Map<String, String> {
        val resultMap = hashMapOf<String, String>()
        val constructors = ElementFilter.constructorsIn(element.enclosedElements)
                .filter { it.enclosedElements.size == 0 }

        if (constructors.size > 1) throw IllegalArgumentException("${element.simpleName} has more then one constructors")

        constructors.first().run {
            parameters.forEach { variable ->
                if (variable.getAnnotation(IgnoreMap::class.java) != null) return@forEach

                val mappingNameAnnotation = variable.getAnnotation(IgnoreMap::class.java)
                if (mappingNameAnnotation != null) {
                    resultMap[variable.simpleName.toString()] = mappingNameAnnotation.value
                    return@forEach
                } else
                    resultMap[variable.simpleName.toString()] = variable.simpleName.toString()

            }
        }

        return resultMap
    }
}