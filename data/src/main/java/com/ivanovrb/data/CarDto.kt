package com.ivanovrb.data

import com.ivanovrb.domain.Car
import com.ivanovrb.domain.User
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName


@Mapper(Car::class)
data class CarDto(
        val mark:Int,
        @MappingName("name") val namedto:String,
        val model:String,
        val id:Int ,
        val user: UserMap
)