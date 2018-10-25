package com.ivanovrb.data

import com.ivanovrb.domain.User
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName

@Mapper(User::class)
data class UserMap(
        val name: String,
        val id: Int
)
