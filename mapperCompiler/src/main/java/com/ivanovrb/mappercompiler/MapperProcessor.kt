package com.ivanovrb.mappercompiler

import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.ElementFilter

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
            if (element.kind != ElementKind.CLASS) throw Throwable("Mapper annotation apply only class")

            val targetClass = element.annotationMirrors.firstOrNull()?.elementValues?.entries?.first()?.value?.value as DeclaredType
            packagesAnnotatedClasses.add(ClassName.bestGuess(element.asType().asTypeName().toString()).packageName)
            graphDependencies[element.asType().asTypeName().toString()] = targetClass.asElement().asType().asTypeName().toString()
            graphDependencies[targetClass.asElement().asType().asTypeName().toString()] = element.asType().asTypeName().toString()
            primaryData[element] = targetClass.asElement()
        }
        generateClass()

        return true
    }

    private fun generateClass() {
        val file = FileSpec.builder("com.ivanovrb.mapper", "MapperExtensions")
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
        val primaryFieldsMap = getConstructorFields(primaryElement, targetElement)
        val targetFieldsMap = getConstructorFields(targetElement, primaryElement)

        if (primaryFieldsMap.values.map { it.first }.toHashSet() != targetFieldsMap.values.map { it.first }.toHashSet())
            throw IllegalArgumentException("${primaryElement.simpleName} fields (${primaryFieldsMap.values.map { it.first }}) and ${targetElement.simpleName} fields (${targetFieldsMap.values.map { it.first }}) are different")

        val parametersStringBuilder = StringBuilder()
        targetFieldsMap.forEach { targetEntry ->
            parametersStringBuilder
                    .append("\t")
                    .append(targetEntry.key)
                    .append(" = ")
                    .append(primaryFieldsMap.entries.find { it.value.first == targetEntry.value.first }?.run { this.value.second })
                    .append(",\n")
        }

        return FunSpec.builder(buildFunctionName(primaryElement.simpleName.toString(), targetElement.simpleName.toString()))
                .addParameter(primaryElement.simpleName.toString().decapitalize(), primaryElement.asType().asTypeName())
                .returns(targetElement.asType().asTypeName())
                .addStatement("with(${primaryElement.simpleName.toString().decapitalize()}){\nreturn ${targetElement.simpleName}(\n${parametersStringBuilder.dropLast(2)}\n)}")
                .build()

    }

    private fun getConstructorFields(primaryElement: Element, targetElement: Element): Map<String, Pair<String, String?>> {
        return if (packagesAnnotatedClasses.contains(ClassName.bestGuess(primaryElement.asType().asTypeName().toString()).packageName))
            getConstructorFieldsFromClassInPackage(primaryElement, targetElement)
        else
            getConstructorFieldsFromClassOutPackage(primaryElement, targetElement)
    }

    private fun getConstructorFieldsFromClassInPackage(element: Element, targetElement: Element): Map<String, Pair<String, String?>> {
        val resultMap = hashMapOf<String, Pair<String, String?>>()
        val constructors = getConstructorFromElement(element)

        constructors.run {
            parameters.forEach { variable ->
                if (variable.getAnnotation(IgnoreMap::class.java) != null) {
                    return@forEach
                }

                resultMap.putAll(extractValues(variable, targetElement))
            }
        }
        return resultMap
    }
//    private fun extractValues(variable: VariableElement, targetElement: Element): Map<out String, Pair<String, String?>>{
//
//    }


    private fun extractValues(variable: VariableElement, targetElement: Element): Map<out String, Pair<String, String?>> {
        val resultMap = hashMapOf<String, Pair<String, String?>>()

        val mappingNameAnnotation = variable.getAnnotation(MappingName::class.java)
        if (mappingNameAnnotation != null) {
            resultMap[variable.simpleName.toString()] = mappingNameAnnotation.value to variable.simpleName.toString()
        } else {
            resultMap[variable.simpleName.toString()] = variable.simpleName.toString() to variable.simpleName.toString()
        }

        val typeVariable = variable.asType()
        val targetType = getConstructorParametersFromElement(targetElement)[variable.simpleName.toString()]
        val isSameType = typeVariable.toString() == targetType.toString()

        if (!MapperUtils.isPrimitive(typeVariable.asTypeName().toString()) && !isSameType) {
            val typeVariableAsElement = (typeVariable as DeclaredType).asElement()
            val mapperType = graphDependencies[typeVariableAsElement.asType().asTypeName().toString()]
                    ?: throw IllegalArgumentException("Class $typeVariable has not mapper method")
            resultMap[variable.simpleName.toString()] = resultMap[variable.simpleName.toString()]!!.first to "map${typeVariableAsElement.simpleName}To${ClassName.bestGuess(mapperType).simpleName}(${variable.simpleName})"
        }

        return resultMap
    }

    private fun getConstructorFieldsFromClassOutPackage(element: Element, targetElement: Element): Map<String, Pair<String, String?>> {
        val resultMap = hashMapOf<String, Pair<String, String?>>()

        val classElement = Class.forName(element.asType().asTypeName().toString()).kotlin
        val constructorParameters = ElementFilter.constructorsIn(element.enclosedElements)
                .first { it.enclosedElements.size == 0 }
                .parameters

        classElement.constructors.first().parameters.forEachIndexed { index, parameter ->

            if (constructorParameters[index].getAnnotation(IgnoreMap::class.java) != null) return@forEachIndexed

            val mappingNameAnnotation = constructorParameters[index].getAnnotation(MappingName::class.java)
            if (mappingNameAnnotation != null) {
                resultMap[parameter.name!!] = mappingNameAnnotation.value to parameter.name!!
            } else {
                resultMap[parameter.name!!] = parameter.name!! to parameter.name!!
            }

            val typeVariable = parameter.type.asTypeName().toString()

            val targetType = getConstructorParametersFromElement(targetElement)[parameter.name!!]
            val isSameType = typeVariable == targetType.toString()

            if (!MapperUtils.isPrimitive(typeVariable) && !isSameType) {
                val mapperType = graphDependencies[typeVariable]
                        ?: throw IllegalArgumentException("Class $typeVariable have not mapper method")
                resultMap[parameter.name!!] = resultMap[parameter.name!!]!!.first to "map${ClassName.bestGuess(typeVariable).simpleName}To${ClassName.bestGuess(mapperType).simpleName}(${parameter.name!!})"
            }
        }

        return resultMap
    }

    private fun getConstructorParametersFromElement(element: Element): Map<String, String> {
        return if (packagesAnnotatedClasses.contains(ClassName.bestGuess(element.asType().asTypeName().toString()).packageName)) {
            getConstructorFromElement(element).run { mapOf(this.simpleName.toString() to this.asType().toString()) }
        } else {
            Class.forName(element.asType().asTypeName().toString()).kotlin.constructors.first().parameters.map { it.name!! to it.type.toString() }.toMap()
        }

    }


    private fun getConstructorFromElement(element: Element): ExecutableElement {
        return ElementFilter.constructorsIn(element.enclosedElements)
                .first { it.enclosedElements.size == 0 }
    }

    private fun buildFunctionName(from: String, to: String): String {
        return "map${from}To$to"
    }

//    private fun buildFunctionName(from: String, to: String): String {
//        return "map${from}To$to"
//    }
}