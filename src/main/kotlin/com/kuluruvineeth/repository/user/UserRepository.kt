package com.kuluruvineeth.repository.user

import com.kuluruvineeth.data.models.User

interface UserRepository {

    suspend fun createUser(user: User)

    suspend fun getUserById(id:String): User?

    suspend fun getUserByEmail(email: String) : User?
}