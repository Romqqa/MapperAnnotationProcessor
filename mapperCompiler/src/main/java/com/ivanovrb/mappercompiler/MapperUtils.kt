package com.ivanovrb.mappercompiler

object MapperUtils{

    private val wrapperTypesKotlin = mapOf(
            Boolean::class.qualifiedName!! to false,
            Char::class.qualifiedName!! to "",
            Int::class.qualifiedName!! to 0,
            Double::class.qualifiedName!! to 0.0,
            Float::class.qualifiedName!! to 0f,
            Byte::class.qualifiedName!! to 0,
            String::class.qualifiedName!! to "\"\""
    )
    private val wrapperTypesJava = mapOf(
            Boolean::class.java.canonicalName!! to false,
            Char::class.java.canonicalName!! to "",
            Int::class.java.canonicalName!! to 0,
            Double::class.java.canonicalName!! to 0.0,
            Float::class.java.canonicalName!! to 0f,
            Byte::class.java.canonicalName!! to 0,
            String::class.java.canonicalName!! to "\"\"",
            "java.lang.Integer" to 0,
            "java.lang.Float" to 0f,
            "java.lang.Double" to 0.0
    )
    fun isPrimitive(value:String):Boolean{
        return wrapperTypesKotlin[value] != null || wrapperTypesJava[value] != null
    }

    fun getDefValue(type:String):Any?{
        return if (wrapperTypesKotlin[type] != null) wrapperTypesKotlin[type] else if(wrapperTypesJava[type] != null) wrapperTypesJava[type] else null
    }
}