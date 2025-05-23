package com.razumly.mvp.core.data.dataTypes

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.razumly.mvp.core.data.dataTypes.crossRef.TeamPlayerCrossRef
import com.razumly.mvp.core.data.dataTypes.crossRef.TournamentUserCrossRef
import com.razumly.mvp.core.data.dataTypes.crossRef.EventUserCrossRef
import kotlinx.serialization.Serializable

@Serializable
data class UserWithRelations(
    @Embedded val user: UserData,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TeamPlayerCrossRef::class,
            parentColumn = "userId",
            entityColumn = "teamId"
        )
    )
    val teams: List<Team>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TournamentUserCrossRef::class,
            parentColumn = "userId",
            entityColumn = "tournamentId"
        )
    )
    val tournaments: List<Tournament>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventUserCrossRef::class,
            parentColumn = "userId",
            entityColumn = "eventId"
        )
    )
    val pickupGames: List<EventImp>
)