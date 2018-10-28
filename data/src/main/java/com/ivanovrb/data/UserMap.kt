package com.ivanovrb.data

import com.ivanovrb.domain.User
import com.ivanovrb.mapper.Default
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName

@Mapper(User::class)
class UserMap(
        val name: String? = "asder",
        @MappingName("ids")val id: Int? = 0

)
