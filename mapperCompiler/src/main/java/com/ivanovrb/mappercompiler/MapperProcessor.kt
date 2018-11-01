package com.ivanovrb.mappercompiler

import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import com.ivanovrb.mappercompiler.factory.ExtractorConstructorFieldsFromClassInPackage
import com.ivanovrb.mappercompiler.factory.ExtractorConstructorFieldsFromClassOutPackage
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class MapperProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION = "kapt.kotlin.generated"
    }

    private val graphDependencies = hashMapOf<String, String>()
    private val primaryData = hashMapOf<Element, Element>()
    private val packagesAnnotatedClasses = hashSetOf<String>()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Mapper::class.java.canonicalName, IgnoreMap::class.java.canonicalName, MappingName::class.java.canonicalName)

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        val annotatedClasses = roundEnvironment?.getElementsAnnotatedWith(Mapper::class.java)

        annotatedClasses?.forEach { element ->
            if (element.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Mapper annotation apply only class")
                return false
            }
            val target = element.annotationMirrors.firstOrNull()?.elementValues?.entries?.first()?.value?.value

            val targetClass = target as DeclaredType
            packagesAnnotatedClasses.add(ClassName.bestGuess(element.asType().asTypeName().toString()).packageName)
            graphDependencies[element.asType().asTypeName().toString()] = targetClass.asElement().asType().asTypeName().toString()
            graphDependencies[targetClass.asElement().asType().asTypeName().toString()] = element.asType().asTypeName().toString()
            primaryData[element] = targetClass.asElement()
        }
        generateClass()

        return true
    }

    private fun generateClass() {
        val fileBuilder = FileSpec.builder("com.ivanovrb.mapper", "MapperExtensions")
        primaryData.forEach {
            fileBuilder
                    .addFunction(buildFunctions(it.key, it.value))
            fileBuilder
                    .addFunction(buildFunctions(it.value, it.key))
        }

        val options = processingEnv.options
        val kotlinGenerated = options[KAPT_KOTLIN_GENERATED_OPTION]
        val mapperFile = File(kotlinGenerated, "mapper")

        mapperFile.mkdir()
        fileBuilder.build().writeTo(mapperFile)
    }

    private fun buildFunctions(primaryElement: Element, targetElement: Element): FunSpec {
        val primaryFieldsMap = getConstructorFields(primaryElement, targetElement)
        val targetFieldsMap = getConstructorFields(targetElement, primaryElement)
        val parametersStringBuilder = StringBuilder()

        if (primaryFieldsMap.values.map { it.first }.toHashSet() != targetFieldsMap.values.map { it.first }.toHashSet()) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${primaryElement.simpleName} fields (${primaryFieldsMap.values.map { it.first }}) and ${targetElement.simpleName} fields (${targetFieldsMap.values.map { it.first }}) are different")
        } else {
            targetFieldsMap.forEach { targetEntry ->
                parametersStringBuilder
                        .append("\t")
                        .append(targetEntry.key)
                        .append(" = ")
                        .append(primaryFieldsMap.entries.find { it.value.first == targetEntry.value.first }?.run { this.value.second })
                        .append(",\n")
            }
        }
        return FunSpec.builder(buildFunctionName(targetElement.simpleName.toString()))
                .receiver(primaryElement.asType().asTypeName())
                .returns(targetElement.asType().asTypeName())
                .addStatement("return ${targetElement.simpleName}(\n${parametersStringBuilder.dropLast(2)}\n)")
                .build()

    }

    private fun getConstructorFields(primaryElement: Element, targetElement: Element): Map<String, Pair<String, String?>> {
        return if (packagesAnnotatedClasses.contains(ClassName.bestGuess(primaryElement.asType().asTypeName().toString()).packageName)){
            ExtractorConstructorFieldsFromClassInPackage(graphDependencies,processingEnv).extract(primaryElement, targetElement)
        } else {
            ExtractorConstructorFieldsFromClassOutPackage(graphDependencies,processingEnv).extract(primaryElement, targetElement)
        }
    }

    private fun buildFunctionName(to: String): String {
        return "mapTo$to"
    }
}
