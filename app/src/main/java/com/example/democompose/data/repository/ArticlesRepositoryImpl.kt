package com.example.democompose.data.repository

import com.example.democompose.data.api.NewsAPI
import com.example.democompose.data.db.ArticlesDAO
import com.example.democompose.data.model.Article
import com.example.democompose.data.model.Articles
import com.example.democompose.data.model.domain.ArticleDomain
import com.example.democompose.data.model.domain.mapArticleToDomain
import com.example.democompose.manager.CredentialManager
import eu.anifantakis.mod.coredata.network.operations.NetworkOperations
import eu.anifantakis.mod.coredata.network.operations.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ArticlesRepositoryImpl @Inject constructor(
    private val newsAPI: NewsAPI,
    private val newsArticleDao: ArticlesDAO,
    private val credentialManager: CredentialManager
) : NetworkOperations(), ArticlesRepository {

    private val coroutineDispatcher = Dispatchers.IO


    // simple example returning the original mapped json result from network
    suspend fun getNetworkArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<Article>>> {
        return withContext(coroutineDispatcher) {

            performNetworkOperation <Articles, Articles, List<Article>>(
                apiRequest = {
                    newsAPI.getNews(
                        query,
                        fromDate,
                        sortBy,
                        credentialManager.getApiKey()
                    )
                },
                processResponse = { response ->
                    // return network response as is
                    response
                }
                // omit domainMapper as in this example we are returning directly without domain mapping
                // Omit cacheUpdate and cacheRefetch parameters since caching is not used
            )
        }
    }

    // simple example with only network api call (no-cache, no domain)
    override suspend fun getArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<Article>>> {
        return withContext(coroutineDispatcher) {

            performNetworkOperation<Articles, List<Article>, List<Article>>(
                apiRequest = {
                    newsAPI.getNews(
                        query,
                        fromDate,
                        sortBy,
                        credentialManager.getApiKey()
                    )
                },
                processResponse = { networResponse ->
                    // map or transform to an intermediate type used for repository operations
                    networResponse.articles ?: emptyList() // Directly process the response
                },
                // omit domainMapper as in this example we are returning directly without domain mapping
                // Omit cacheUpdate and cacheRefetch parameters since caching is not used
            )
        }
    }

    // simple example with only network api call (no-cache)
    override suspend fun getDomainArticlesNoCache(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>> {
        return withContext(coroutineDispatcher) {
            performNetworkOperation(
                apiRequest = {
                    newsAPI.getNews(
                        query,
                        fromDate,
                        sortBy,
                        credentialManager.getApiKey()
                    )
                },
                processResponse = { networkResponse ->
                    // map or transform to an intermediate type used for repository operations
                    networkResponse.articles ?: emptyList() // Directly process the response
                },
                // domainMapper is optional; it's used to map the result to a domain object
                domainMapper = { articlesList ->
                    // map to domain object
                    articlesList.map { mapArticleToDomain(it) }
                }
                // Omit cacheUpdate and cacheRefetch parameters since caching is not used
            )
        }
    }

    // full example with cache and exception handling
    override suspend fun getDomainArticlesCached(query: String, fromDate: String, sortBy: String): Flow<NetworkResult<List<ArticleDomain>>> {
        return withContext(coroutineDispatcher) {
            performNetworkOperation<Articles, List<Article>, List<ArticleDomain>>  (
                apiRequest = {
                    newsAPI.getNews(
                        query,
                        fromDate,
                        sortBy,
                        credentialManager.getApiKey()
                    )
                },
                cacheFetch = { newsArticleDao.getArticles() },
                cacheUpdate = { networkResponse ->
                    // Process the response and update the cache
                    val fetchedArticles = networkResponse.articles ?: emptyList()
                    newsArticleDao.saveArticles(fetchedArticles) // Save fetched articles to database

                    val fetchedArticleUrls = fetchedArticles.map { it.url }
                    newsArticleDao.deleteArticlesNotIn(fetchedArticleUrls) // Keep only fetched articles in database
                },
                cacheRefetch = { newsArticleDao.getArticles() }, // Refetch from cache after network update
                domainMapper = { articlesList ->
                     articlesList.map { mapArticleToDomain(it) }
                },
                onFetchFailed = { exception ->
                    Timber.e(exception)
                }
            )
        }
    }
}
