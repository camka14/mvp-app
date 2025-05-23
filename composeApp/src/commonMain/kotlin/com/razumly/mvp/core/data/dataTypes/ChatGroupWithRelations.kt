package com.razumly.mvp.core.data.dataTypes

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.razumly.mvp.core.data.dataTypes.crossRef.ChatUserCrossRef
import kotlinx.serialization.Serializable

@Serializable
data class ChatGroupWithRelations (
    @Embedded val chatGroup: ChatGroup,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ChatUserCrossRef::class,
            parentColumn = "chatId",
            entityColumn = "userId"
        )
    )
    val users: List<UserData>,

    @Relation(
        parentColumn = "id",
        entityColumn = "chatId",
        entity = MessageMVP::class
    )
    val messages: List<MessageMVP>
)