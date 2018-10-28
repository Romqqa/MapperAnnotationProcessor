package com.ivanovrb.mappercompiler.factory

import com.squareup.kotlinpoet.TypeName
import com.sun.tools.javac.code.AnnoConstruct
import com.sun.tools.javac.code.Attribute
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.VariableElement

class Constructor(
        val parameters:List<Parameter>
)

data class Parameter (
        val simpleName:String,
        val typeName:TypeName,
        private val annotationMirrors: List<AnnotationMirror>
): AnnoConstruct(){
    override fun getAnnotationMirrors(): com.sun.tools.javac.util.List<out Attribute.Compound> = annotationMirrors as com.sun.tools.javac.util.List<out Attribute.Compound>
}

data class Type(
        val name:String
)