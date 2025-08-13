package com.brocoders.taaza_khabar.data.api


import com.brocoders.taaza_khabar.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String,
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String
    ): Response<NewsResponse>

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20
    ): Response<NewsResponse>

    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val API_KEY = "290065c3f01e41629a392036a5b1c4af"
    }
}