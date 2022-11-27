package com.kuluruvineeth.routes

import com.kuluruvineeth.service.ChatService
import com.kuluruvineeth.util.Constants
import com.kuluruvineeth.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach


fun Route.getMessagesForChat(chatService:ChatService){
    authenticate {
        get("/api/chat/messages"){
            val chatId = call.parameters[QueryParams.PARAM_CHAT_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE

            if(!chatService.doesChatBelongToUser(chatId,call.userId)){
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val messages = chatService.getMessagesForChat(chatId,page,pageSize)
            call.respond(HttpStatusCode.OK,messages)
        }
    }
}

fun Route.getChatsForUser(chatService: ChatService){
    authenticate {
        get("/api/chats"){
            val chats = chatService.getChatsForUser(call.userId)
            call.respond(HttpStatusCode.OK,chats)
        }
    }
}

fun Route.chatWebSocket(chatService: ChatService){
    webSocket("/api/chat/webSocket") {
        incoming.consumeEach { frame ->
            when(frame){
                is Frame.Text -> {
                    if(frame.readText() == "Hello World"){
                        send(Frame.Text("Yo, what's up"))
                    }
                }

                is Frame.Binary -> TODO()
                is Frame.Close -> TODO()
                is Frame.Ping -> TODO()
                is Frame.Pong -> TODO()
            }
        }
    }
}