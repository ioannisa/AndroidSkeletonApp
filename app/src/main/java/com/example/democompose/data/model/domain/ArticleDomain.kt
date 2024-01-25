package com.example.democompose.data.model.domain

import androidx.compose.runtime.Immutable
import com.example.democompose.data.model.Article
import java.util.UUID
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

fun mapArticleToDomain(article: Article): ArticleDomain {
    return ArticleDomain(
        id = abs(article.url.hashCode()).toString(),
        url = article.url,
        author = article.author,
        content = article.content,
        publishedAt = article.publishedAt,
        title = article.title,
        urlToImage = article.urlToImage
    )
}

