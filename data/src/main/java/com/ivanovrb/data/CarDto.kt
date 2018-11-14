package com.ivanovrb.data

import com.ivanovrb.domain.Car
import com.ivanovrb.domain.User
import com.ivanovrb.mapper.Default
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName


@Mapper(Car::class)
data class CarDto(
        val mark:Float? = 0f,
        @MappingName("name") val namedto:String? = "ASd",
        val model:String? = "ves",
        @Default ("3") val id:Int? = 1,
//        @Default("UserMap(null, 3)")
        @MappingName("user")val user: UserMap? = null
){

}