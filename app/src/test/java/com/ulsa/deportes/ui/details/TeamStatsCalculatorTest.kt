package com.ulsa.deportes.ui.details

import com.ulsa.deportes.ui.details.util.calculateTeamStats
import com.ulsa.deportes.ui.homeSection.homeHome.model.*
import org.junit.Assert.*
import org.junit.Test

class TeamStatsCalculatorTest {

    private val teamA = Team(1, "Águilas", "CDMX", "AGU", "")
    private val teamB = Team(2, "Búhos", "MTY", "BUH", "")
    private val teamC = Team(3, "Tigres", "GDL", "TIG", "")

    @Test
    fun testMatchesPlayedAndWinsLossesDraws() {
        val matches = listOf(
            MatchScore(1, "Fútbol", "Finalizado", "2026-03-01T15:00:00Z", teamA, teamB, ScoreResult(3, 1), ""),
            MatchScore(2, "Fútbol", "Finalizado", "2026-03-02T15:00:00Z", teamB, teamA, ScoreResult(2, 2), ""),
            MatchScore(3, "Fútbol", "Finalizado", "2026-03-03T15:00:00Z", teamA, teamC, ScoreResult(0, 1), "")
        )

        val stats = calculateTeamStats("Águilas", matches)
        assertEquals(3, stats.matchesPlayed)
        assertEquals(1, stats.wins)
        assertEquals(1, stats.draws)
        assertEquals(1, stats.losses)
        assertEquals(5, stats.goalsFor)
        assertEquals(4, stats.goalsAgainst)
    }

    @Test
    fun testUnfinishedMatchesIgnored() {
        val matches = listOf(
            MatchScore(1, "Fútbol", "Finalizado", "2026-03-01T15:00:00Z", teamA, teamB, ScoreResult(2, 0), ""),
            MatchScore(2, "Fútbol", "En curso", "2026-03-05T15:00:00Z", teamA, teamC, ScoreResult(1, 1), "")
        )

        val stats = calculateTeamStats("Águilas", matches)
        assertEquals(1, stats.matchesPlayed)
        assertEquals(1, stats.wins)
        assertEquals(2, stats.goalsFor)
        assertEquals(0, stats.goalsAgainst)
    }

    @Test
    fun testTeamWithNoMatches() {
        val matches = listOf(
            MatchScore(1, "Fútbol", "Finalizado", "2026-03-01T15:00:00Z", teamB, teamC, ScoreResult(1, 0), "")
        )

        val stats = calculateTeamStats("Águilas", matches)
        assertEquals(0, stats.matchesPlayed)
        assertEquals(0, stats.wins)
        assertEquals(0, stats.draws)
        assertEquals(0, stats.losses)
        assertEquals(0, stats.goalsFor)
        assertEquals(0, stats.goalsAgainst)
    }

    @Test
    fun testHomeAndAwayMatches() {
        val matches = listOf(
            MatchScore(1, "Fútbol", "Finalizado", "2026-03-01T15:00:00Z", teamA, teamB, ScoreResult(3, 2), ""),
            MatchScore(2, "Fútbol", "Finalizado", "2026-03-02T15:00:00Z", teamC, teamA, ScoreResult(1, 4), "")
        )

        val stats = calculateTeamStats("Águilas", matches)
        assertEquals(2, stats.matchesPlayed)
        assertEquals(2, stats.wins)
        assertEquals(7, stats.goalsFor)
        assertEquals(3, stats.goalsAgainst)
    }
}
