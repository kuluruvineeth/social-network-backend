package com.kuluruvineeth.routes

import com.google.gson.Gson
import com.kuluruvineeth.data.webSocket.WsServerMessage
import com.kuluruvineeth.service.chat.ChatController
import com.kuluruvineeth.service.chat.ChatService
import com.kuluruvineeth.service.chat.ChatSession
import com.kuluruvineeth.util.Constants
import com.kuluruvineeth.util.QueryParams
import com.kuluruvineeth.util.WebSocketObject
import com.kuluruvineeth.util.fromJsonOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.koin.java.KoinJavaComponent.inject


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
        val session = call.sessions.get<ChatSession>()
        if(session == null){
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"No session"))
            return@webSocket
        }
        chatController.onJoin(session,this)
        try {
            incoming.consumeEach { frame ->

                kotlin.run {
                    when(frame){
                        is Frame.Text -> {
                            val frameText = frame.readText()
                            val delimiterIndex = frameText.indexOf("#")
                            if(delimiterIndex == -1){
                                println("No delimiter found")
                                return@run
                            }
                            val type = frameText.substring(0,delimiterIndex).toIntOrNull()
                            if(type == null){
                                println("Invalid format")
                                return@run
                            }
                            val json = frameText.substring(delimiterIndex+1,frameText.length)
                            handleWebSocket(this,session,chatController,type,frameText,json)
                        }

                        else -> Unit
                    }
                }

            }
        }catch (e: Exception){
            e.printStackTrace()
        }finally {
            println("Disconnecting #$session")
            chatController.onDisconnect(session.userId)
        }
    }
}

suspend fun handleWebSocket(
    webSocketSession: WebSocketSession,
    session: ChatSession,
    chatController: ChatController,
    type: Int,
    frameText: String,
    json: String
){
    val gson by inject<Gson>(Gson::class.java)
    when(type){
        WebSocketObject.MESSAGE.ordinal -> {
            val message = gson.fromJsonOrNull(json,WsServerMessage::class.java) ?: return
            chatController.sendMessage(frameText,message)
        }
    }
}