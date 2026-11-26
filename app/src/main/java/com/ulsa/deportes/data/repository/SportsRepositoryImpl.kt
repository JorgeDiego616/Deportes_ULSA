package com.ulsa.deportes.data.repository

import com.ulsa.deportes.domain.repository.SportsRepository
import com.ulsa.deportes.ui.homeSection.homeHome.model.*
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeService
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementación de [SportsRepository] utilizando Retrofit [HomeService] con soporte centralizado de respaldo (fallback)
 * ante indisponibilidad de red (IOException) y errores de servidor HTTP 5xx,
 * asegurando no ocultar errores 404, autenticación ni incompatibilidades de modelos JSON.
 */
class SportsRepositoryImpl(
    private val homeService: HomeService
) : SportsRepository {
    override suspend fun getHomeData(): HomeData {
        return try {
            homeService.getHomeData()
        } catch (e: HttpException) {
            when (e.code()) {
                401, 403, 404 -> throw e // Nunca usar fallback para 401, 403 ni 404
                else -> {
                    if (e.code() in 500..599 || e.code() == 503) {
                        getFallbackHomeData()
                    } else {
                        throw e
                    }
                }
            }
        } catch (_: IOException) {
            // Fallback únicamente para indisponibilidad de red / servidor
            getFallbackHomeData()
        }
    }

    private fun getFallbackHomeData(): HomeData {
        return HomeData(
            scores = listOf(
                MatchScore(
                    id = 1,
                    sport = "Fútbol [Demostración]",
                    status = "Finalizado",
                    playedAt = "2026-03-01T15:00:00Z",
                    homeTeam = Team(1, "Águilas ULSA", "CDMX", "AGU", ""),
                    awayTeam = Team(2, "Búhos La Salle", "MTY", "BUH", ""),
                    score = ScoreResult(3, 1),
                    summary = "Victoria contundente (Datos de demostración / respaldo por indisponibilidad del servidor)"
                ),
                MatchScore(
                    id = 2,
                    sport = "Básquetbol [Demostración]",
                    status = "Finalizado",
                    playedAt = "2026-03-02T17:00:00Z",
                    homeTeam = Team(3, "Tigres", "GDL", "TIG", ""),
                    awayTeam = Team(4, "Toros CUU", "CUU", "TOR", ""),
                    score = ScoreResult(88, 85),
                    summary = "Partido cerrado definido en los últimos segundos (Datos de demostración / respaldo)"
                )
            ),
            news = listOf(
                NewsItem(
                    id = 1,
                    title = "¡Victoria de los Linces! [Demostración]",
                    shortDescription = "Gran actuación del equipo representativo de fútbol en el torneo interuniversitario.",
                    content = "El equipo representativo de fútbol de la Universidad La Salle obtuvo una victoria clave este fin de semana en un encuentro disputado en el Estadio Principal. (Información de demostración por respaldo de servidor).",
                    image = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800",
                    author = "Deportes ULSA (Demo)",
                    publishedAt = "2026-03-03T10:00:00Z"
                ),
                NewsItem(
                    id = 2,
                    title = "Nuevo horario de Gimnasio [Demostración]",
                    shortDescription = "Consulta los nuevos horarios de entrenamiento y acondicionamiento físico.",
                    content = "A partir de la próxima semana, el gimnasio universitario ampliará su horario matutino y vespertino. (Información de demostración por respaldo de servidor).",
                    image = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800",
                    author = "Coordinación Deportiva (Demo)",
                    publishedAt = "2026-03-02T08:00:00Z"
                )
            ),
            events = listOf(
                SportEvent(
                    id = 1,
                    title = "Torneo Relámpago de Fútbol 7 [Demostración]",
                    sport = "Fútbol",
                    image = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800",
                    startDate = "2026-03-15T09:00:00Z",
                    endDate = "2026-03-15T18:00:00Z",
                    venue = "Estadio La Salle",
                    announcement = "Inscripciones abiertas para facultades. (Evento de demostración / respaldo).",
                    rules = listOf("Credencial vigente", "Equipo de 7 a 10 jugadores")
                )
            )
        )
    }
}
