package com.ivanovrb.mappercompiler

import com.ivanovrb.mappercompiler.factory.ExtractorConstructorFieldsFromClassInPackage
import com.ivanovrb.mappercompiler.factory.ExtractorConstructorFieldsFromClassOutPackage
import com.squareup.kotlinpoet.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements
import javax.tools.Diagnostic
import javax.tools.StandardLocation

class MapperProcessor : AbstractProcessor() {

    private val graphDependencies = hashMapOf<String, String>()
    private val primaryData = hashMapOf<Element, Element>()
    private val packagesAnnotatedClasses = hashSetOf<String>()
    private var elementUtils:Elements? = null

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(MAPPER_CLASS_NAME)

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (roundEnvironment?.processingOver() == true) return false

        elementUtils = processingEnv.elementUtils

        val annotatedClasses = roundEnvironment?.getElementsAnnotatedWith(elementUtils?.getTypeElement(MAPPER_CLASS_NAME))

        annotatedClasses?.forEach { element ->
            if (element.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Mapper annotation apply only class")
                return false
            }

            val target = element.annotationMirrors.findLast { it.annotationType.toString() == MAPPER_CLASS_NAME}?.elementValues?.entries?.first()?.value?.value

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
        val fileBuilder = FileSpec.builder(packagesAnnotatedClasses.firstOrNull()?:"com.ivanovrb.mapper", "MapperExtensions")

        primaryData.forEach {
            fileBuilder
                    .addFunction(buildFunctions(it.key, it.value))
            fileBuilder
                    .addFunction(buildFunctions(it.value, it.key))
        }

        try {
            val writer = processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, fileBuilder.packageName, "MapperExtensions.kt").openWriter()
            fileBuilder.build().writeTo(writer)
            writer.close()
        } catch (e:Exception){
        }
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
            ExtractorConstructorFieldsFromClassInPackage(elementUtils!!, graphDependencies,processingEnv).extract(primaryElement, targetElement)
        } else {
            ExtractorConstructorFieldsFromClassOutPackage(elementUtils!!, graphDependencies,processingEnv).extract(primaryElement, targetElement)
        }
    }

    private fun buildFunctionName(to: String): String {
        return "mapTo$to"
    }
}
