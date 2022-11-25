package com.kuluruvineeth.plugins

import com.kuluruvineeth.repository.user.UserRepository
import com.kuluruvineeth.routes.createUserRoute
import com.kuluruvineeth.routes.loginUser
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository: UserRepository by inject()
    routing {
        createUserRoute(userRepository)
        loginUser(userRepository)
    }
}
