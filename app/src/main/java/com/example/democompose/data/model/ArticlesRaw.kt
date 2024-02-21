package com.example.democompose.data.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ArticlesRaw(
    @Json(name = "articles")
    val articles: List<ArticleRaw>?,

    @Json(name = "status")
    val status: String,

    @Json(name = "totalResults")
    val totalResults: Int
)