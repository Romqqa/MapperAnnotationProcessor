package com.ivanovrb.domain

import android.os.Parcelable
import com.ivanovrb.domain.User
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.Mapper
import com.ivanovrb.mapper.MappingName
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

@Parcelize
data class Car(
        val id:Int = 0,
        val name:String,
        val mark:Float,
        val model:String,
        val user: User?,
        @IgnoreMap val isMain:Boolean = true
): Parcelable{
    companion object {
        const val ASFAEF= 21
    }
    private val testVal:String = ""
    private val testVal2:String = ""
}

