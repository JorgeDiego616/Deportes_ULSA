package com.ulsa.deportes.domain.repository

import com.ulsa.deportes.ui.homeSection.homeHome.model.HomeData

/**
 * Interfaz de repositorio para datos deportivos (partidos, noticias, eventos).
 * Desacopla los ViewModels de los clientes de red concretos.
 */
interface SportsRepository {
    suspend fun getHomeData(): HomeData
}
