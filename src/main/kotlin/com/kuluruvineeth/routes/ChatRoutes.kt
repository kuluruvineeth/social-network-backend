package com.kuluruvineeth.routes

import com.kuluruvineeth.service.chat.ChatController
import com.kuluruvineeth.service.chat.ChatService
import com.kuluruvineeth.service.chat.ChatSession
import com.kuluruvineeth.util.Constants
import com.kuluruvineeth.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach


fun Route.getMessagesForChat(chatService: ChatService){
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

fun Route.chatWebSocket(chatController: ChatController){
    webSocket("/api/chat/webSocket") {
        val session = call.sessions.get("SESSION") as? ChatSession
        if(session == null){
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"No session"))
            return@webSocket
        }
        try {
            incoming.consumeEach { frame ->
                when(frame){
                    is Frame.Text -> {

                    }

                    is Frame.Binary -> TODO()
                    is Frame.Close -> TODO()
                    is Frame.Ping -> TODO()
                    is Frame.Pong -> TODO()
                }
            }
        }catch (e: Exception){

        }finally {
            chatController.onDisconnect(session.userId)
        }
    }
}