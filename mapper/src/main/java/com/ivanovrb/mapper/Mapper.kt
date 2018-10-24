package com.ivanovrb.mapper

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Mapper(val value:KClass<*>)