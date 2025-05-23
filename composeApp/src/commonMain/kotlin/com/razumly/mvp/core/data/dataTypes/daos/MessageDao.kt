package com.razumly.mvp.core.data.dataTypes.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.razumly.mvp.core.data.dataTypes.MessageMVP

@Dao
interface MessageDao {
    @Upsert
    suspend fun upsertMessages(messageMVPs: List<MessageMVP>)

    @Upsert
    suspend fun upsertMessage(messageMVP: MessageMVP)

    @Delete
    suspend fun deleteMessage(messageMVP: MessageMVP)

    @Query("DELETE FROM MessageMVP WHERE id IN (:ids)")
    suspend fun deleteMessages(ids: List<String>)

    @Query("DELETE FROM MessageMVP WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("SELECT * FROM MessageMVP WHERE id = :id")
    suspend fun getMessageById(id: String): MessageMVP?

    @Query("SELECT * FROM MessageMVP WHERE chatId = :chatGroupId")
    suspend fun getMessagesInChatGroup(chatGroupId: String): List<MessageMVP>
}