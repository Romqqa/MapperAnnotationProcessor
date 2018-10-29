package com.ivanovrb.mapperannotationprocessor.data

import com.ivanovrb.mapper.Default
import com.ivanovrb.mapper.Mapper


data class Cat(
        val name:String
)

@Mapper(Cat::class)
data class CatDto(
        val name:String = "asd"
)