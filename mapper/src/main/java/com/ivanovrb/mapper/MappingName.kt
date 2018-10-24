package com.ivanovrb.mapper

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER,
        AnnotationTarget.FIELD,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.LOCAL_VARIABLE)
annotation class MappingName(val value :String)