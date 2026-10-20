package com.ulsa.deportes.data.repository

import com.ulsa.deportes.domain.repository.SportsRepository
import com.ulsa.deportes.ui.homeSection.homeHome.model.HomeData
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeService

/**
 * Implementación de [SportsRepository] utilizando Retrofit [HomeService].
 */
class SportsRepositoryImpl(
    private val homeService: HomeService
) : SportsRepository {
    override suspend fun getHomeData(): HomeData {
        return homeService.getHomeData()
    }
}
