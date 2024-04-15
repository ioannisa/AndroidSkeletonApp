package com.example.democompose.data.model.domain

import androidx.compose.runtime.Immutable
import com.example.democompose.data.db.ArticleDB
import com.example.democompose.data.model.ArticleRaw
import kotlin.math.abs

// demo for domain
@Immutable
data class ArticleDomain(
    val id: String,
    val url: String,
    val author: String?,
    val content: String,
    val publishedAt: String,
    val title: String,
    val urlToImage: String?
) {
    // not necessary, more for demo... but simpler "equals" might
    // improve marginally performance in compose when using "key"
    override fun equals(other: Any?): Boolean {
        return other is ArticleDomain && other.id == this.id
    }

    override fun hashCode(): Int {
        return "$id$url$author$content$publishedAt$title$urlToImage".hashCode()
    }
}


// MAPPERS
fun ArticleRaw.toDb(): ArticleDB {
    return ArticleDB(
        url = this.url,
        author = this.author,
        content = this.content,
        description = this.description,
        publishedAt = this.publishedAt,
        title = this.title ?: "",
        urlToImage = this.urlToImage
    )
}

fun ArticleDB.toDomain(): ArticleDomain {
    return ArticleDomain(
        id = abs(this.url.hashCode()).toString(),
        url = this.url,
        author = this.author,
        content = this.content,
        publishedAt = this.publishedAt,
        title = this.title,
        urlToImage = this.urlToImage
    )
}

fun ArticleRaw.toDomain(): ArticleDomain {
    val articleDB = this.toDb()
    return articleDB.toDomain()
}
