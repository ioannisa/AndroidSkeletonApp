package com.example.democompose.data.repository

import com.example.democompose.data.model.Article
import com.example.democompose.data.model.domain.ArticleDomain
import com.example.democompose.network.operations.NetworkResult
import kotlinx.coroutines.flow.Flow

interface ArticlesRepository {
    // Example implementations for the same query using offline cache or not
    suspend fun getDomainArticlesCached(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>>
    suspend fun getDomainArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>>
    suspend fun getArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<Article>>>
}