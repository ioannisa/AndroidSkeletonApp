package com.example.democompose.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article")
data class ArticleDB(
    val author: String?,
    val content: String,
    val description: String?,
    val publishedAt: String,
    val title: String,

    @PrimaryKey
    @ColumnInfo(name = "url") // custom columns in database (just for reference)
    val url: String,

    val urlToImage: String?
)