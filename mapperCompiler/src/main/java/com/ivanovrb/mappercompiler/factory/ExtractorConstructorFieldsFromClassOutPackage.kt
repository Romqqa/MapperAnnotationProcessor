package com.ivanovrb.mappercompiler.factory

import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements


class ExtractorConstructorFieldsFromClassOutPackage(
        private val elementsUtils: Elements,
        graphDependencies: Map<String, String>,
        processingEnv: ProcessingEnvironment
) : ExtractorConstructorFields(elementsUtils, graphDependencies, processingEnv) {

    override val primaryConstructor: Constructor by lazy {
            val constructorsAsElement = getConstructorsFromElement(primaryElement)
            val classElement = Class.forName(primaryElement.asType().asTypeName().toString()).kotlin

            val constructors = classElement.constructors
                    .filter { it.parameters.isNotEmpty() }
                    .filter { it.parameters[0].type.toString() != "android.os.Parcel" }
                    .mapIndexed { index, kFunction ->
                        Constructor(
                                constructorsAsElement[index].parameters.mapIndexed { indexParameter, parameter ->
                                    val isNullable = parameter.annotationMirrors.firstOrNull { Nullable::class.qualifiedName == it.annotationType.toString() } != null
                                    Parameter(kFunction.parameters[indexParameter].name!!,if (isNullable) parameter.typeName.asNullable() else parameter.typeName, parameter.annotationMirrors, parameter.simpleName)
                                },
                                constructorsAsElement[index].annotationMirrors)
                    }
                    .toList()

            if (constructors.size == 1)
                constructors.first()
            else
                resolveConstructorsConflict(constructors)
        }
}