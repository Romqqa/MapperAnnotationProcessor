package com.ivanovrb.mapperannotationprocessor.data

import com.ivanovrb.mapper.Mapper

class Cat(
        val name:String
)

@Mapper(Cat::class)
class CatDto(
        val name:String
)