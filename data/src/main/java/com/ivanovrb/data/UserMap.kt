package com.ivanovrb.data

import com.ivanovrb.domain.User
import com.ivanovrb.mapper.Default
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import java.util.Date

@Mapper(User::class)
class UserMap(
        @MappingName("ids") val id: Int? = 1,
        val name: String? = "vswer",
        val date: Date? = null,
        val list:List<String>? = null,
        @IgnoreMap val ignore :Int = 123
)
