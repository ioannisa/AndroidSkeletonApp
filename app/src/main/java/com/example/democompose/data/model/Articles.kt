package com.example.democompose.data.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Articles(
    @Json(name = "articles")
    val articles: List<Article>?,

    @Json(name = "status")
    val status: String,

    @Json(name = "totalResults")
    val totalResults: Int
)