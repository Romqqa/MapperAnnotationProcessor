package com.ivanovrb.mappercompiler

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.sun.org.apache.xpath.internal.operations.Bool

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
        java.lang.Integer::class.java.canonicalName to 0,
        java.lang.Double::class.java.canonicalName to 0,
        java.lang.Float::class.java.canonicalName!! to 0f,
        java.lang.Byte::class.java.canonicalName!! to 0,
        java.lang.String::class.java.canonicalName!! to "\"\""
)
private val wrapperTypesJavaToKotlin = mapOf(
        Boolean::class.java.canonicalName!! to Boolean::class.asTypeName(),
        Char::class.java.canonicalName!! to Char::class.asTypeName(),
        java.lang.Integer::class.java.canonicalName to Int::class.asTypeName(),
        java.lang.Double::class.java.canonicalName to Double::class.asTypeName(),
        java.lang.Float::class.java.canonicalName!! to Float::class.asTypeName(),
        java.lang.Byte::class.java.canonicalName!! to Byte::class.asTypeName(),
        java.lang.String::class.java.canonicalName!! to String::class.asTypeName()
)

object MapperUtils {

    fun isPrimitive(value: String): Boolean {
        return wrapperTypesKotlin[value] != null || wrapperTypesJava[value] != null
    }

    fun getDefValue(type: String): Any? {
        return if (wrapperTypesKotlin[type] != null) wrapperTypesKotlin[type] else if (wrapperTypesJava[type] != null) wrapperTypesJava[type] else null
    }
}

fun TypeName.isPrimitive():Boolean{
    return wrapperTypesKotlin[this.asNonNullable().toString()] != null || wrapperTypesJava[this.asNonNullable().toString()] != null
}

fun TypeName.asKotlinPrimitive(): TypeName? {
    return when {
        wrapperTypesKotlin[this.asNonNullable().toString()] != null -> return this
        wrapperTypesJava[this.asNonNullable().toString()] != null -> return wrapperTypesJavaToKotlin[this.asNonNullable().toString()]!!
        else -> null
    }
}