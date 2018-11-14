package com.ivanovrb.data

import com.ivanovrb.domain.User
import com.ivanovrb.mapper.*
import java.util.Date

@Mapper(User::class)
class UserMap(
          val id: Int = 1,
        val name: String = "vswer",
        val date: Date = Date(),
        val list:List<String>? = null,
        @IgnoreMap val ignore :Int = 123
){
    @MappingConstructor
    constructor(
            @MappingName("ids")id:Int?,
            name: String?
    ) : this( id?:0, name?:"")
}
