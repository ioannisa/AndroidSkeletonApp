package com.example.democompose.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "article")
@JsonClass(generateAdapter = true)
data class Article(

    @Json(name = "author")
    val author: String?,

    @Json(name = "content")
    val content: String,

    @Json(name = "description")
    val description: String?,

    @Json(name = "publishedAt")
    val publishedAt: String,

    @Json(name = "title")
    val title: String,

    @PrimaryKey
    @ColumnInfo(name = "url") // custom columns in database (just for reference)
    @Json(name = "url")
    val url: String,

    @Json(name = "urlToImage")
    val urlToImage: String?
)