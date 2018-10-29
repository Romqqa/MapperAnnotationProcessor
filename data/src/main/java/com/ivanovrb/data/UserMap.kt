package com.ivanovrb.data

import com.ivanovrb.domain.User
import com.ivanovrb.mapper.Default
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName

@Mapper(User::class)
class UserMap(
        @MappingName("ids") val id: Int? = 1,
        val name: String? = "vswer",
        @IgnoreMap val ignore :Int = 123
)
