package com.example.democompose.data.api

import com.example.democompose.data.model.Articles
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("v2/everything")
    suspend fun getNews(
        @Query("q")         query:      String,
        @Query("from")      fromDate:   String,
        @Query("sortBy")    sortBy:     String,
        @Query("apiKey")    apiKey:     String
    ): Response<Articles>
}