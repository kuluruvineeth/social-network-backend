package com.kuluruvineeth.service.chat

import com.kuluruvineeth.data.models.Message
import com.kuluruvineeth.repository.chat.ChatRepository
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

class ChatController(
    private val chatRepository: ChatRepository
) {

    private val onlineUsers = ConcurrentHashMap<String,WebSocketSession>()

    fun onJoin(chatSession: ChatSession,socket: WebSocketSession){
        onlineUsers[chatSession.userId] = socket
    }

    fun onDisconnect(userId: String){
        if(onlineUsers.containsKey(userId)){
            onlineUsers.remove(userId)
        }
    }

    suspend fun sendMessage(message: Message){
        onlineUsers[message.fromId]?.send(Frame.Text(message.text))
        chatRepository.insertMessage(message)
    }
}