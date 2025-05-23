package com.razumly.mvp.eventDetail.data

import com.razumly.mvp.core.data.dataTypes.MatchMVP
import com.razumly.mvp.core.data.dataTypes.MatchWithRelations
import com.razumly.mvp.core.data.repositories.IMVPRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface IMatchRepository : IMVPRepository {
    suspend fun getMatch(matchId: String): Result<MatchMVP>
    fun getMatchFlow(matchId: String): Flow<Result<MatchWithRelations>>
    suspend fun updateMatch(match: MatchMVP): Result<Unit>
    fun getMatchesOfTournamentFlow(tournamentId: String): Flow<Result<List<MatchWithRelations>>>
    suspend fun updateMatchFinished(match: MatchMVP, time: Instant): Result<Unit>
    suspend fun getMatchesOfTournament(tournamentId: String): Result<List<MatchMVP>>
    suspend fun subscribeToMatches(): Result<Unit>
    suspend fun unsubscribeFromRealtime(): Result<Unit>
    fun setIgnoreMatch(match: MatchMVP?): Result<Unit>
}