package com.kuluruvineeth.service

import com.kuluruvineeth.data.models.User
import com.kuluruvineeth.data.requests.CreateAccountRequest
import com.kuluruvineeth.repository.user.UserRepository

class UserService(
    private val repository: UserRepository
){

    suspend fun doesUserWithEmailExist(email:String): Boolean{
        return repository.getUserByEmail(email) != null
    }

    suspend fun createUser(request: CreateAccountRequest){
        repository.createUser(
            User(
                email = request.email,
                username = request.username,
                password = request.password,
                profileImageUrl = "",
                bio = "",
                githubUrl = null,
                instagramUrl = null,
                linkedInUrl = null
            )
        )
    }

    fun validateCreateAccountRequest(request: CreateAccountRequest) : ValidationEvent{
        if(request.email.isBlank() || request.password.isBlank() || request.username.isBlank()){
            return ValidationEvent.ErrorFieldEmpty
        }
        return ValidationEvent.Success
    }

    sealed class ValidationEvent{
        object ErrorFieldEmpty : ValidationEvent()
        object Success : ValidationEvent()
    }
}