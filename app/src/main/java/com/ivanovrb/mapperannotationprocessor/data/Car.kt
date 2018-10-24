package com.ivanovrb.mapperannotationprocessor.data

import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import org.jetbrains.annotations.NotNull

data class Car(
        val id:Int,
        val name:String,
        val mark:Int,
        val model:String,
        @IgnoreMap val isMain:Boolean = true
)

//@Mapper(Car::class)
data class CarDto(
        val mark:Int,
        @MappingName("name") val namedto:String,
        val model:String,
        val id:Int
)