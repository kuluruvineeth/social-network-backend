package com.kuluruvineeth.plugins

import com.kuluruvineeth.service.chat.ChatSession
import io.ktor.server.application.*
import io.ktor.server.application.*
import io.ktor.server.sessions.*


fun Application.configureSessions(){
    install(Sessions){
        cookie<ChatSession>("SESSION")
    }
}