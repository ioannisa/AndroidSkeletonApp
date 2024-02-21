package com.example.democompose.data.repository

import com.example.democompose.data.model.ArticleRaw
import com.example.democompose.data.model.ArticlesRaw
import com.example.democompose.data.model.domain.ArticleDomain
import eu.anifantakis.mod.coredata.network.operations.NetworkResult
import kotlinx.coroutines.flow.Flow

interface ArticlesRepository {

    // Just Network, no cache, no domain mapping
    suspend fun getNetworkArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<ArticlesRaw>>

    // Example implementations for the same query using offline cache or not
    suspend fun getDomainArticlesCached(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>>
    suspend fun getDomainArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>>
}