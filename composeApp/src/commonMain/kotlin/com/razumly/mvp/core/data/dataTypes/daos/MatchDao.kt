package com.razumly.mvp.core.data.dataTypes.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.razumly.mvp.core.data.dataTypes.MatchMVP
import com.razumly.mvp.core.data.dataTypes.MatchWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Upsert
    suspend fun upsertMatch(match: MatchMVP)

    @Upsert
    suspend fun upsertMatches(matches: List<MatchMVP>)

    @Delete
    suspend fun deleteMatch(match: MatchMVP)

    @Query("SELECT COUNT(*) FROM MatchMVP")
    suspend fun getTotalMatchCount(): Int

    @Query("SELECT * FROM MatchMVP WHERE tournamentId = :tournamentId")
    suspend fun getMatches(tournamentId: String): List<MatchMVP>

    @Query("DELETE FROM MatchMVP WHERE id IN (:ids)")
    suspend fun deleteMatchesById(ids: List<String>)

    @Transaction
    @Query("SELECT * FROM MatchMVP WHERE id = :id")
    fun getMatchFlowById(id: String): Flow<MatchWithRelations>

    @Transaction
    @Query("SELECT * FROM MatchMVP WHERE id = :id")
    suspend fun getMatchById(id: String): MatchWithRelations?

    @Transaction
    @Query("SELECT * FROM MatchMVP WHERE tournamentId = :tournamentId")
    fun getMatchesByTournamentId(tournamentId: String): Flow<List<MatchWithRelations>>
}