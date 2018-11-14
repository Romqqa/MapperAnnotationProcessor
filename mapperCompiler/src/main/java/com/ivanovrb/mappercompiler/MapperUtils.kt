package com.ivanovrb.mappercompiler

import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.sun.org.apache.xpath.internal.operations.Bool

const val MAPPER_CLASS_NAME = "com.ivanovrb.mapper.Mapper"
const val IGNORE_MAP_CLASS_NAME = "com.ivanovrb.mapper.IgnoreMap"
const val MAPPING_CONTUCTOR_CLASS_NAME = "com.ivanovrb.mapper.MappingConstructor"
const val MAPPING_NAME_CLASS_NAME = "com.ivanovrb.mapper.MappingName"
const val DEFAULT_CLASS_NAME = "com.ivanovrb.mapper.Default"

private val wrapperTypesKotlin = mapOf(
        Boolean::class.qualifiedName!! to false,
        Char::class.qualifiedName!! to "",
        Int::class.qualifiedName!! to 0,
        Long::class.qualifiedName!! to 0,
        Double::class.qualifiedName!! to 0.0,
        Float::class.qualifiedName!! to 0f.toString(),
        Byte::class.qualifiedName!! to 0,
        String::class.qualifiedName!! to "\"\""
)
private val wrapperTypesJava = mapOf(
        Boolean::class.java.canonicalName!! to false,
        Char::class.java.canonicalName!! to "",
        java.lang.Integer::class.java.canonicalName to 0,
        java.lang.Long::class.java.canonicalName to 0,
        java.lang.Double::class.java.canonicalName to 0.0,
        java.lang.Float::class.java.canonicalName!! to "0f",
        java.lang.Byte::class.java.canonicalName!! to 0,
        java.lang.String::class.java.canonicalName!! to "\"\""
)
private val wrapperTypesJavaToKotlin = mapOf(
        Boolean::class.java.canonicalName!! to Boolean::class.asTypeName(),
        Char::class.java.canonicalName!! to Char::class.asTypeName(),
        java.lang.Integer::class.java.canonicalName to Int::class.asTypeName(),
        java.lang.Long::class.java.canonicalName to Long::class.asTypeName(),
        java.lang.Double::class.java.canonicalName to Double::class.asTypeName(),
        java.lang.Float::class.java.canonicalName!! to Float::class.asTypeName(),
        java.lang.Byte::class.java.canonicalName!! to Byte::class.asTypeName(),
        java.lang.String::class.java.canonicalName!! to String::class.asTypeName()
)

private val collections: Map<String, String> by lazy {
    mapOf(
            List::class.java.canonicalName to "listOf()",
            Map::class.java.canonicalName to "mapOf()",
            Set::class.java.canonicalName to "setOf()"
    )
}

object MapperUtils {

    fun isPrimitive(value: String): Boolean {
        return wrapperTypesKotlin[value] != null || wrapperTypesJava[value] != null
    }

    fun getDefValue(type: String): Any? {
        return if (wrapperTypesKotlin[type] != null) wrapperTypesKotlin[type] else if (wrapperTypesJava[type] != null) wrapperTypesJava[type] else null
    }

    fun asKotlinPrimitive(type: String): String? {
        return when {
            wrapperTypesKotlin[type] != null -> return type
            wrapperTypesJava[type] != null -> return wrapperTypesJavaToKotlin[type]!!.canonicalName
            else -> null
        }
    }
}

fun TypeName.isPrimitive(): Boolean {
    return wrapperTypesKotlin[this.asNonNullable().toString()] != null || wrapperTypesJava[this.asNonNullable().toString()] != null
}

fun TypeName.asKotlinPrimitive(): TypeName? {
    return when {
        wrapperTypesKotlin[this.asNonNullable().toString()] != null -> return this
        wrapperTypesJava[this.asNonNullable().toString()] != null -> return wrapperTypesJavaToKotlin[this.asNonNullable().toString()]!!
        else -> null
    }
}

fun TypeName.getStubCollection(): String? {
    val collection = collections[this.asNonNullable().toString().substringBefore("<")]
    if (collection != null && this is ParameterizedTypeName) {
        val types = this.typeArguments.map { it.asKotlinPrimitive() ?: it }.toString()
        return collection.replace("()", "<${types.substring(1, types.length - 1)}>")
    }
    return collection
}

fun String.append(action: () -> Any): String {
    return "$this ${action()}"
}
fun String.appendLn(action: () -> Any): String {
    return "$this\n${action().toString()}"
}