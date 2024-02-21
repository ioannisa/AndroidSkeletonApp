package com.example.democompose.data.repository

import com.example.democompose.data.api.NewsAPI
import com.example.democompose.data.db.ArticleDB
import com.example.democompose.data.db.ArticlesDAO
import com.example.democompose.data.model.ArticleRaw
import com.example.democompose.data.model.ArticlesRaw
import com.example.democompose.data.model.domain.ArticleDomain
import com.example.democompose.data.model.domain.toDb
import com.example.democompose.data.model.domain.toDomain
import com.example.democompose.manager.CredentialManager
import eu.anifantakis.mod.coredata.network.operations.NetworkOperations
import eu.anifantakis.mod.coredata.network.operations.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ArticlesRepositoryImpl @Inject constructor(
    private val newsAPI: NewsAPI,
    private val newsArticleDao: ArticlesDAO,
    private val credentialManager: CredentialManager
) : NetworkOperations(), ArticlesRepository {

    private val coroutineDispatcher = Dispatchers.IO

    suspend fun offlineArticlesFlow(): Flow<List<ArticleDB>> {
        return newsArticleDao.getArticles()
    }

    // simple example returning the original mapped json result from network
    override suspend fun getNetworkArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<ArticlesRaw>> {
        return withContext(coroutineDispatcher) {

            netop <ArticlesRaw> (
                apiRequest = {
                     newsAPI.getNews(
                        query,
                        fromDate,
                        sortBy,
                        credentialManager.getApiKey()
                    )
                }
            )
        }
    }

    // simple example with only network api call (no-cache)
    override suspend fun getDomainArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>> {
        return withContext(coroutineDispatcher) {
            netopDomain <ArticlesRaw, List<ArticleDomain>> (
                apiRequest = {
                    newsAPI.getNews(
                        query,
                        fromDate,
                        sortBy,
                        credentialManager.getApiKey()
                    )
                },
                // domainMapper is optional; it's used to map the result to a domain object
                apiToDomain = { articlesList ->
                    // map to domain object
                    articlesList.articles?.map { it.toDomain() } ?: emptyList()
                }
            )
        }
    }

    // full example with cache and exception handling
    override suspend fun getDomainArticlesCached(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>> {
        return withContext(coroutineDispatcher) {

            // the three types don't need explicit declaration, they can be obtained automatically
            //
            // <ArticlesRaw>:         auto-obtained by the apiRequest (retrofit Response type)
            // <List<ArticleDB>>:     auto-obtained by the cacheFetch (room return type)
            // <List<ArticleDomain>>: auto-obtained by the return type of the host function's NetworkResult

            netopCachedDomain <ArticlesRaw, List<ArticleDB>, List<ArticleDomain>> (
                cacheFetch = { newsArticleDao.getArticles() },
                apiRequest = {
                    newsAPI.getNews(
                        query,
                        fromDate,
                        sortBy,
                        credentialManager.getApiKey()
                    )
                },
                cacheUpdate = { networkResponse ->
                    // Process the response and update the cache
                    val networkArticles = networkResponse.articles ?: emptyList()

                    val dbArticles = networkArticles.map {it.toDb() }
                    newsArticleDao.saveArticles(dbArticles) // Save fetched articles to database

                    val fetchedArticleUrls = networkArticles.map { it.url }
                    newsArticleDao.deleteArticlesNotIn(fetchedArticleUrls) // Keep only fetched articles in database
                },
                cacheRefetch = { newsArticleDao.getArticles() }, // Refetch from cache after network update
                cacheToDomain = { articlesList ->
                     articlesList.map { it.toDomain() }
                },
                onFetchFailed = { exception ->
                    Timber.e(exception)
                }
            )
        }
    }
}