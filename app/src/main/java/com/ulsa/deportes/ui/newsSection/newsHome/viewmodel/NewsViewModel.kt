package com.ulsa.deportes.ui.newsSection.newsHome.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulsa.deportes.ui.newsSection.newsHome.model.NewsItem
import com.ulsa.deportes.ui.newsSection.newsHome.network.NewsApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsViewModel : ViewModel() {
    var newsList by mutableStateOf<List<NewsItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val retrofit = Retrofit.Builder() //retrofit libreria para hacer peticiones a apis
        .baseUrl("https://gist.githubusercontent.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
//se hace el fetch de las noticias y carga datos
    private val apiService = retrofit.create(NewsApiService::class.java)

    fun fetchNews(gistRawUrl: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Usamos la URL completa del Gist Raw
                newsList = apiService.getNews(gistRawUrl)
            } catch (e: Exception) {
                errorMessage = "Error al cargar noticias: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}