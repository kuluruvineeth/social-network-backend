package com.kuluruvineeth.service.chat

import com.google.gson.Gson
import com.kuluruvineeth.data.webSocket.WsClientMessage
import com.kuluruvineeth.data.webSocket.WsServerMessage
import com.kuluruvineeth.repository.chat.ChatRepository
import com.kuluruvineeth.util.WebSocketObject
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

class ChatController(
    private val chatRepository: ChatRepository
) {

    private val onlineUsers = ConcurrentHashMap<String,WebSocketSession>()

    fun onJoin(userId: String,socket: WebSocketSession){
        onlineUsers[userId] = socket
    }

    fun onDisconnect(userId: String){
        if(onlineUsers.containsKey(userId)){
            onlineUsers.remove(userId)
        }
    }

    suspend fun sendMessage(ownUserId:String,gson:Gson, message: WsClientMessage){
        val messageEntity = message.toMessage(ownUserId)
        val wsServerMessage = WsServerMessage(
            fromId = ownUserId,
            toId = message.toId,
            text = message.text,
            timestamp = System.currentTimeMillis(),
            chatId = message.chatId
        )
        val frameText = gson.toJson(wsServerMessage)
        onlineUsers[ownUserId]?.send(Frame.Text("${WebSocketObject.MESSAGE.ordinal}#$frameText"))
        onlineUsers[message.toId]?.send(Frame.Text("${WebSocketObject.MESSAGE.ordinal}#$frameText"))
        chatRepository.insertMessage(messageEntity)
        if(!chatRepository.doesChatByUserExist(ownUserId,message.toId)){
            chatRepository.insertChat(ownUserId,message.toId,messageEntity.id)
        }else{
            message.chatId?.let {
                chatRepository.updateLastMessageIdForChat(message.chatId,messageEntity.id)
            }
        }
    }
}