package com.ivanovrb.domain

import android.os.Parcelable
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.MappingConstructor
import com.ivanovrb.mapper.MappingName
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull
import java.util.*

@Parcelize
data class User(
        val id: Int = 12,
        val name: String = "",
        val date:Date? = null,
        val list:List<Car> = listOf()
) : Parcelable {
}