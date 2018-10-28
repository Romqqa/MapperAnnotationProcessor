package com.ivanovrb.mappercompiler.factory

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element


class ExtractorConstructorFieldsFromClassInPackage(
        graphDependencies:Map<String, String>,
        processingEnv: ProcessingEnvironment
)  : ExtractorConstructorFields(graphDependencies, processingEnv) {

    override val primaryConstructor: Constructor
        get() = getConstructorFromElement(primaryElement).run { Constructor(this.parameters.map { Parameter(it.simpleName.toString(), it.asType().asTypeName(), it.annotationMirrors) })}

    override fun getTargetTypeOfVariable(variable: Parameter):TypeName? = targetConstructorParameter[variable.simpleName]

    override fun getConstructorParametersFromElement(element: Element): Map<String, TypeName> {
        return getConstructorFromElement(element).parameters.map { it.simpleName.toString() to it.asType().asTypeName() }.toMap()
    }

}