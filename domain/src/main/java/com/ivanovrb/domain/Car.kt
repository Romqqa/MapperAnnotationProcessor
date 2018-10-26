package com.ivanovrb.domain

import com.ivanovrb.domain.User
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import org.jetbrains.annotations.NotNull

data class Car(
        val id:Int = 0,
        val name:String,
        val mark:Float,
        val model:String,
        val user: User?,
        @IgnoreMap val isMain:Boolean = true
)