package com.ivanovrb.data

import com.ivanovrb.domain.User

class DataSource{
    fun getUser(userMap: UserMap):User{
        return Mapper.mapUserMapToUser(userMap)
    }
}