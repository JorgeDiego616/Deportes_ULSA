package com.ulsa.deportes.ui.homeSection.homeHome.model

data class Team(
    val id: Int,
    val name: String,
    val city: String,
    val abbreviation: String,
    val logo: String
)

data class ScoreResult(val home: Int, val away: Int)

data class MatchScore(
    val id: Int,
    val sport: String,
    val status: String,
    val playedAt: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val score: ScoreResult,
    val summary: String
)

data class NewsItem(
    val id: Int,
    val title: String,
    val shortDescription: String,
    val content: String,
    val image: String,
    val author: String,
    val publishedAt: String
)

data class SportEvent(
    val id: Int,
    val title: String,
    val sport: String,
    val image: String,
    val startDate: String,
    val endDate: String,
    val venue: String,
    val announcement: String,
    val rules: List<String>
)

data class HomeData(
    val scores: List<MatchScore>,
    val news: List<NewsItem>,
    val events: List<SportEvent>
)

data class HomeResponse(
    val scores: List<MatchScore>,
    val news: List<NewsItem>,
    val events: List<SportEvent>
)