package com.kuluruvineeth.plugins

import com.kuluruvineeth.service.chat.ChatSession
import io.ktor.server.application.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.sessions.*
import io.ktor.util.*


fun Application.configureSessions(){
    install(Sessions){
        cookie<ChatSession>("SESSION")
    }
    intercept(Plugins){
        if(call.sessions.get<ChatSession>() == null){
            val userId = call.parameters["userId"] ?: return@intercept
            call.sessions.set(ChatSession(userId, generateNonce()))
        }
    }
}