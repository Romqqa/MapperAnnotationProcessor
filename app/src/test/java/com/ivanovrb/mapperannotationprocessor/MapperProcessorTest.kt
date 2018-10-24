package com.ivanovrb.mapperannotationprocessor

import com.ivanovrb.mapperannotationprocessor.data.User
import com.ivanovrb.mapperannotationprocessor.data.UserDto
import org.junit.Assert.*
import org.junit.Test

class MapperProcessorTest {

    @Test
    fun testUserMapToUser() {
        val userDto = UserDto(1,"name")
        val user= User(1, "name")
        assertEquals(user, Mapper.mapUserDtoToUser(userDto))
        assertEquals(userDto, Mapper.mapUserToUserDto(user))
    }
}
