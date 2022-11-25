package com.kuluruvineeth.routes

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic
import com.kuluruvineeth.repository.user.UserRepository
import com.kuluruvineeth.data.models.User
import com.kuluruvineeth.data.requests.CreateAccountRequest
import com.kuluruvineeth.data.requests.LoginRequest
import com.kuluruvineeth.data.responses.BasicApiResponse
import com.kuluruvineeth.util.ApiResponseMessages
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createUserRoute(userRepository: UserRepository){
    route("/api/user/create"){
        post {
            val request = kotlin.runCatching { call.receiveNullable<CreateAccountRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userExists = userRepository.getUserByEmail(request.email) != null
            print("UserExists $userExists")
            if(userExists){
                call.respond(
                    BasicApiResponse(
                        successful = false,
                        message = ApiResponseMessages.USER_ALREADY_EXISTS
                    )
                )
                return@post
            }
            if(request.email.isBlank() || request.password.isBlank() || request.username.isBlank()){
                call.respond(
                    BasicApiResponse(
                        successful = false,
                        message = ApiResponseMessages.FIELDS_BLANK
                    )
                )
                return@post
            }
            userRepository.createUser(
                User(
                    email = request.email,
                    username = request.username,
                    password = request.password,
                    profileImageUrl = "",
                    bio = "",
                    githubUrl = null,
                    linkedInUrl = null,
                    instagramUrl = null
                )
            )
            call.respond(
                BasicApiResponse(successful = true)
            )
        }
    }
}


fun Route.loginUser(userRepository: UserRepository){
    post("/api/user/login"){
        val request = kotlin.runCatching { call.receiveNullable<LoginRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        if(request.email.isBlank() || request.password.isBlank()){
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val isCorrectPassword = userRepository.doesPasswordForUserMatch(
            email = request.email,
            enteredPassword = request.password
        )

        if(isCorrectPassword){
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = true
                )
            )
        }else{
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = false,
                    message = ApiResponseMessages.INVALID_CREDENTIALS
                )
            )
        }

    }
}