package com.ivanovrb.domain

import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.MappingName
import org.jetbrains.annotations.NotNull

data class User(
        @MappingName("id") val ids: Int,
        val name: String
)