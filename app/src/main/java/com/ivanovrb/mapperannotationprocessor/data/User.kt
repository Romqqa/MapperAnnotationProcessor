package com.ivanovrb.mapperannotationprocessor.data

import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import org.jetbrains.annotations.NotNull

data class User(
        val id: Int,
        val name: String
)

@Mapper(User::class)
data class UserDto(
        val id: Int,
        @MappingName("name")  val name: String
)