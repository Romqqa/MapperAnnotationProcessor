package com.ivanovrb.data

import com.ivanovrb.domain.User
import com.ivanovrb.mapper.mapToUser

class DataSource{
    fun getUser(userMap: UserMap):User{
        return userMap.mapToUser()
    }
}