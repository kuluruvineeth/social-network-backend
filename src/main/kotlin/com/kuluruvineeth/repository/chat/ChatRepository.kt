package com.kuluruvineeth.repository.chat

import com.kuluruvineeth.data.models.Chat
import com.kuluruvineeth.data.models.Message
import com.kuluruvineeth.data.responses.ChatDto

interface ChatRepository {

    suspend fun getMessagesForChat(chatId: String,page:Int,pageSize:Int) : List<Message>

    suspend fun getChatsForUser(ownUserId:String): List<ChatDto>

    suspend fun doesChatBelongToUser(chatId: String,userId:String): Boolean

    suspend fun insertMessage(message: Message)

    suspend fun insertChat(userId1: String, userId2: String, messageId: String):String

    suspend fun doesChatByUserExist(userId1:String,userId2: String): Boolean

    suspend fun updateLastMessageIdForChat(chatId: String, lastMessageId: String)
}