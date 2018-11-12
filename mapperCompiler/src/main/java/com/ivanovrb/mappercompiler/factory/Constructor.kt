package com.ivanovrb.mappercompiler.factory

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
//import com.sun.tools.javac.code.AnnoConstruct
//import com.sun.tools.javac.code.Attribute
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.AnnotationMirror

class Constructor(
        val parameters:List<Parameter> = arrayListOf(),
        private val annotationMirrors: List<AnnotationMirror>
): AnnotatedConstruct{

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotationsByType(p0: Class<A>?): Array<A>? {
        return annotationMirrors.filter { it.annotationType.asTypeName() == p0?.asTypeName() }.toTypedArray() as Array<A>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotation(p0: Class<A>?): A? {
       return annotationMirrors.find { it.annotationType.asTypeName() == p0?.asTypeName() } as A?
    }

    override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
        return annotationMirrors.toMutableList()
    }
//    override fun getAnnotationMirrors(): com.sun.tools.javac.util.List<out Attribute.Compound> = annotationMirrors as com.sun.tools.javac.util.List<out Attribute.Compound>
}

data class Parameter (
        val simpleName:String,
        val typeName:TypeName,
        private val annotationMirrors: List<AnnotationMirror>,
        val argName:String = ""
): AnnotatedConstruct{
    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotationsByType(p0: Class<A>?): Array<A>? {
        return annotationMirrors.filter { it.annotationType.asTypeName() == p0?.asTypeName() }.toTypedArray() as Array<A>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotation(p0: Class<A>?): A? {
        return annotationMirrors.find { it.annotationType.asTypeName() == p0?.asTypeName() } as A?
    }

    override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
        return annotationMirrors.toMutableList()
    }
}
