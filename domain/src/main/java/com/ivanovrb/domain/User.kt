package com.ivanovrb.domain

import android.os.Parcelable
import com.ivanovrb.mapper.IgnoreMap
import com.ivanovrb.mapper.MappingConstructor
import com.ivanovrb.mapper.MappingName
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

@Parcelize
data class User(
        val ids: Int = 12,
        val name: String = "sa"
) : Parcelable {
    constructor(name:String) : this(2, name)
//    @MappingConstructor constructor(ids:Int?, name: String?) : this(ids?:0, name?:"")
}