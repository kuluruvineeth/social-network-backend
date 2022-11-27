package com.kuluruvineeth.service.chat

import com.kuluruvineeth.data.webSocket.WsServerMessage
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

    suspend fun sendMessage(frameText: String, message: WsServerMessage){
        onlineUsers[message.fromId]?.send(Frame.Text(frameText))
        onlineUsers[message.toId]?.send(Frame.Text(frameText))
        val messageEntity = message.toMessage()
        chatRepository.insertMessage(messageEntity)
        if(!chatRepository.doesChatByUserExist(message.fromId,message.toId)){
            chatRepository.insertChat(message.fromId,message.toId,messageEntity.id)
        }else{
            message.chatId?.let {
                chatRepository.updateLastMessageIdForChat(message.chatId,messageEntity.id)
            }
        }
    }
}