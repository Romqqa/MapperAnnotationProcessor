package com.ivanovrb.mappercompiler.resolver

import com.ivanovrb.mappercompiler.MAPPING_CONTUCTOR_CLASS_NAME
import com.ivanovrb.mappercompiler.MAPPING_NAME_CLASS_NAME
import com.ivanovrb.mappercompiler.asKotlinPrimitive
import com.ivanovrb.mappercompiler.factory.Constructor
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic


class ConstructorConflictResolver(
        private val processingEnv: ProcessingEnvironment,
        private val element: Element
) {
    fun resolve(constructors: List<Constructor>, targetConstructors: List<Constructor>): Constructor {

        val annotatedConstructor = findAnnotatedConstructor(constructors)
        if (annotatedConstructor != null)
            return annotatedConstructor

        return findSameConstructor(constructors, targetConstructors)
    }

    private fun findAnnotatedConstructor(constructors: List<Constructor>): Constructor? {
        val annotatedConstructors = constructors
                .filter { it.annotationMirrors.firstOrNull { MAPPING_CONTUCTOR_CLASS_NAME == it.annotationType.toString() } != null }

//        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING,element.simpleName.toString() + " " +  annotatedConstructors.toString())
        if (annotatedConstructors.isNotEmpty()) {
            if (annotatedConstructors.size > 1)
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "In ${element.simpleName} find more than one annotated constructors. Give first annotated constructor.")
            return annotatedConstructors.first()
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "In ${element.simpleName} find more than one constructors, but not found annotated with @MappingConstructor")
        }
        return null
    }

    private fun findSameConstructor(constructors: List<Constructor>, targetConstructors: List<Constructor>): Constructor {
        val targetConstructorMaxParameters = targetConstructors
                .asSequence()
                .map { it ->
                    it.parameters.map {
                        it.typeName.asKotlinPrimitive()
                    }
                }
                .toHashSet()

        val sameSizeConstructors = constructors
                .asSequence()
                .sortedBy { it.parameters.size }
                .filter { constructor ->
                    targetConstructorMaxParameters.find { list ->
                        list == constructor.parameters.map {
                             it.typeName.asKotlinPrimitive()
                        }
                    } != null
                }
                .toList()

        if (sameSizeConstructors.isEmpty()) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "In ${element.simpleName} not found constructors for ${targetConstructors.map { constructor ->
                constructor.parameters.map {
                    val name = it.annotationMirrors
                            .firstOrNull { MAPPING_NAME_CLASS_NAME == it.annotationType.toString() }
                            ?.elementValues
                            ?.filterKeys { "value" == it.simpleName.toString() }
                            ?.mapValues { it.value.value as kotlin.String }
                            ?.toList()
                            ?.firstOrNull()
                            ?.second
                    (name ?: it.simpleName) + " " + it.typeName.asKotlinPrimitive()
                }
            }}")
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "In ${element.simpleName} choose constructors for ${sameSizeConstructors.map { constructor -> constructor.parameters.map { it.simpleName + " " + it.typeName } }.first()}")
            return sameSizeConstructors.first()
        }
        return constructors.first()
    }

}