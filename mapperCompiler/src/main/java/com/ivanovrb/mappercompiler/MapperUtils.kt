package com.ivanovrb.mappercompiler

object MapperUtils{

    private val wrapperTypesKotlin = setOf(
            Boolean::class.qualifiedName!!,
            Char::class.qualifiedName!!,
            Int::class.qualifiedName!!,
            Double::class.qualifiedName!!,
            Float::class.qualifiedName!!,
            Byte::class.qualifiedName!!,
            String::class.qualifiedName!!
    )
    private val wrapperTypesJava = setOf(
            Boolean::class.java.canonicalName!!,
            Char::class.java.canonicalName!!,
            Int::class.java.canonicalName!!,
            Double::class.java.canonicalName!!,
            Float::class.java.canonicalName!!,
            Byte::class.java.canonicalName!!,
            String::class.java.canonicalName!!
    )
    fun isPrimitive(value:String):Boolean{
        return wrapperTypesKotlin.contains(value) || wrapperTypesJava.contains(value)
    }
}