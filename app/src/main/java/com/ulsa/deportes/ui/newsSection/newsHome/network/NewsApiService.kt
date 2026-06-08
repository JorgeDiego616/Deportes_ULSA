package com.ulsa.deportes.ui.newsSection.newsHome.network

import com.ulsa.deportes.ui.newsSection.newsHome.model.NewsItem
import retrofit2.http.GET
import retrofit2.http.Url

interface NewsApiService {
    @GET
    suspend fun getNews(@Url url: String): List<NewsItem>
}