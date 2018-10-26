package com.ivanovrb.domain

import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.MappingName
import org.jetbrains.annotations.NotNull

data class User(
        @MappingName("ids") val ids: Int = 1,
        val name: String = "vswer"
)