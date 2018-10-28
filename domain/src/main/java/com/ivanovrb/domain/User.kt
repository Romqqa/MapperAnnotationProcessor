package com.ivanovrb.domain

import android.os.Parcelable
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.MappingName
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

@Parcelize
data class User(
        val ids: Int = 1,
        val name: String = "vswer"
) : Parcelable {
//    constructor(name:String) : this(2, name)
}