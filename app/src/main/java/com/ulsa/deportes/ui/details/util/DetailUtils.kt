package com.ulsa.deportes.ui.details.util

import com.ulsa.deportes.ui.homeSection.homeHome.model.MatchScore
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatea una fecha ISO a formato largo en español (ej. "7 de septiembre de 2026").
 */
fun formatLongSpanishDate(isoDate: String): String {
    return try {
        val datePart = isoDate.substringBefore("T")
        val localDate = LocalDate.parse(datePart)
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        localDate.format(formatter)
    } catch (_: Exception) {
        isoDate
    }
}

data class TeamStats(
    val matchesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val matches: List<MatchScore>
)

/**
 * Calcula las estadísticas de un equipo a partir de los partidos finalizados de forma robusta
 * (mediante normalización y coincidencia parcial/completa por nombre o abreviación).
 */
fun calculateTeamStats(teamQuery: String, matches: List<MatchScore>): TeamStats {
    val query = teamQuery.normalize()
    val teamMatches = matches.filter { match ->
        match.homeTeam.name.normalize().contains(query) ||
        match.homeTeam.abbreviation.normalize().contains(query) ||
        match.awayTeam.name.normalize().contains(query) ||
        match.awayTeam.abbreviation.normalize().contains(query)
    }

    var played = 0
    var wins = 0
    var draws = 0
    var losses = 0
    var goalsFor = 0
    var goalsAgainst = 0

    val finishedMatches = teamMatches.filter { match ->
        val status = match.status.normalize()
        status.contains("final") || status.contains("terminado") || status.contains("finished") ||
        (!status.contains("live") && !status.contains("en vivo") && !status.contains("curso") && !status.contains("proximo") && !status.contains("upcoming"))
    }

    for (match in finishedMatches) {
        val isHome = match.homeTeam.name.normalize().contains(query) || match.homeTeam.abbreviation.normalize().contains(query)
        val myScore = if (isHome) match.score.home else match.score.away
        val opponentScore = if (isHome) match.score.away else match.score.home

        played++
        goalsFor += myScore
        goalsAgainst += opponentScore

        if (myScore > opponentScore) {
            wins++
        } else if (myScore == opponentScore) {
            draws++
        } else {
            losses++
        }
    }

    return TeamStats(
        matchesPlayed = played,
        wins = wins,
        draws = draws,
        losses = losses,
        goalsFor = goalsFor,
        goalsAgainst = goalsAgainst,
        matches = teamMatches
    )
}

private fun String.normalize(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "").lowercase(Locale.ROOT)
}
