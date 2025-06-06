package com.razumly.mvp.core.data.dataTypes.dtos

import com.razumly.mvp.core.data.dataTypes.MatchMVP
import com.razumly.mvp.core.data.dataTypes.enums.Division
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MatchDTO(
    @Transient
    val id: String = "",
    val matchId: Int,
    val team1: String?,
    val team2: String?,
    val tournamentId: String,
    val refId: String?,
    val field: String?,
    val start: String,
    val end: String?,
    val division: String,
    val team1Points: List<Int>,
    val team2Points: List<Int>,
    val losersBracket: Boolean,
    val winnerNextMatchId: String?,
    val loserNextMatchId: String?,
    val previousLeftId: String?,
    val previousRightId: String?,
    val setResults: List<Int>,
    val refereeCheckedIn: Boolean?,
)

fun MatchDTO.toMatch(id: String): MatchMVP {
    return MatchMVP(
        id = id,
        tournamentId = tournamentId,
        team1 = team1,
        team2 = team2,
        matchNumber = matchId,
        refId = refId,
        field = field,
        start = Instant.parse(start),
        end = end?.let { Instant.parse(it) },
        division = Division.valueOf(division),
        team1Points = team1Points,
        team2Points = team2Points,
        losersBracket = losersBracket,
        setResults = setResults,
        refCheckedIn = refereeCheckedIn,
        winnerNextMatchId = winnerNextMatchId,
        loserNextMatchId = loserNextMatchId,
        previousLeftMatchId = previousLeftId,
        previousRightMatchId = previousRightId,
    )
}