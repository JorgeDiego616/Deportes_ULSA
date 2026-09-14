package com.ulsa.deportes.ui.homeSection.homeHome.network

import com.ulsa.deportes.ui.homeSection.homeHome.model.HomeData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface HomeService {
    // Nota: este es un endpoint REST normal (Django REST Framework), NO pasa
    // por el gateway GraphQL — es un servicio externo aparte (Render.com).
    @GET("api/home/sports/")
    suspend fun getHomeData(): HomeData
}

object HomeRetrofitClient {
    private const val BASE_URL = "https://androidbasics-auth-api.onrender.com/"

    val service: HomeService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HomeService::class.java)
    }
}