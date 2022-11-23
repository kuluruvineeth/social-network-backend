package com.kuluruvineeth.controller.user

import com.kuluruvineeth.data.models.User

interface UserController {

    suspend fun createUser(user: User)

    suspend fun getUserById(id:String): User?

    suspend fun getUserByEmail(email: String) : User?
}