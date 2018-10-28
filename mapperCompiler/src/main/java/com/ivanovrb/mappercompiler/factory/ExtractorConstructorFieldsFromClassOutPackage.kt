package com.ivanovrb.mappercompiler.factory

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element


class ExtractorConstructorFieldsFromClassOutPackage(
        graphDependencies: Map<String, String>,
        processingEnv: ProcessingEnvironment
) : ExtractorConstructorFields(graphDependencies, processingEnv) {

    override val primaryConstructor: Constructor
        get() {
            val classElement = Class.forName(primaryElement.asType().asTypeName().toString()).kotlin
            val constructorParameters = getConstructorFromElement(primaryElement)
                    .parameters

            return classElement.constructors
                    .filter { it.parameters[0].type.toString() != "android.os.Parcel" }
                    .asSequence()
                    .map { Constructor(it.parameters.mapIndexed { index, kParameter -> Parameter(kParameter.name!!, kParameter.type.asTypeName(), constructorParameters[index].annotationMirrors) }) }
                    .first()
        }

    override fun getTargetTypeOfVariable(variable: Parameter): TypeName? = targetConstructorParameter[variable.simpleName]

    override fun getConstructorParametersFromElement(element: Element): Map<String, TypeName> {
        return Class.forName(element.asType().asTypeName().toString()).kotlin.constructors.first().parameters.map { it.name!! to it.type.asTypeName() }.toMap()
    }

}