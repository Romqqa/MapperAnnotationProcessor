package com.ivanovrb.mappercompiler.factory

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment


class ExtractorConstructorFieldsFromClassInPackage(
        graphDependencies: Map<String, String>,
        processingEnv: ProcessingEnvironment
) : ExtractorConstructorFields(graphDependencies, processingEnv) {

    override val primaryConstructor: Constructor by lazy {
            val constructors = getConstructorsFromElement(primaryElement)

             if (constructors.size == 1)
                constructors.first()
            else
                resolveConstructorsConflict(constructors)
        }
//    override val targetConstructor: Constructor
//        get() {
////            val classElement = Class.forName(targetElement.asType().asTypeName().toString()).kotlin.constructors.first().parameters.map { it.name!! to it.type.toString() }.toMap()
//            val constructorsAsElement = getConstructorsFromElement(targetElement)
//            val classElement = try {
//                Class.forName(targetElement.asType().asTypeName().toString()).kotlin
//            } catch (e:ClassNotFoundException){
//                null
//            }
//
////            var indexParameter = 0
//            val constructors = classElement?.let {
//                it.constructors
//                        .filter { it.parameters[0].type.toString() != "android.os.Parcel" }
//                        .filter { it.parameters.isNotEmpty() }
//                        .mapIndexed { index, kFunction ->
//                            Constructor(
//                                    constructorsAsElement[index].parameters.mapIndexed { indexParameter, parameter ->
//                                        val isNullable = parameter.getAnnotation(Nullable::class.java) != null
//                                        Parameter(kFunction.parameters[indexParameter].name!!, if (isNullable) parameter.typeName.asNullable() else parameter.typeName, parameter.annotationMirrors)
//                                    },
//                                    constructorsAsElement[index].annotationMirrors)
//                        }
//                        .toList()
//            } ?: constructorsAsElement
//
////            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, classElement.simpleName + " " +  constructors.map { it.parameters.map { it.typeName.nullable } }.toString())
//            return if (constructors.size == 1)
//                constructors.first()
//            else
//                resolveConstructorsConflict(constructors)
//        }
//    override val targetConstructor: Constructor
//        get() {
//            val constructors = getConstructorsFromElement(targetElement)
//
//            return if (constructors.size == 1)
//                constructors.first()
//            else
//                resolveConstructorsConflict(constructors)
//        }
}



