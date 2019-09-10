package com.ivanovrb.mappercompiler.factory

import com.ivanovrb.mappercompiler.append
import com.ivanovrb.mappercompiler.appendLn
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Elements
import javax.lang.model.util.SimpleElementVisitor8
import javax.tools.Diagnostic
import kotlin.reflect.KParameter


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
                                Parameter(kFunction.parameters[indexParameter].name!!, if (isNullable) parameter.typeName.copy(nullable = true) else parameter.typeName, parameter.annotationMirrors, parameter.simpleName)
                            },
                            constructorsAsElement[index].annotationMirrors)
                }
                .toList()

        if (constructors.size == 1)
            constructors.first()
        else {
            resolveConstructorsConflict(constructors)
        }
    }
}