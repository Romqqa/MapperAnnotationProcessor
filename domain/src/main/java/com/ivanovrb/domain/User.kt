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
        val ids: Int = 12,
        val name: String = "sa",
        val date:Date = Date(),
        val list:List<String> = listOf()
) : Parcelable {
//    @MappingConstructor constructor(ids:Int?, name: String?) : this(ids?:0, name?:"")
}