package com.example.democompose.manager

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.democompose.data.model.domain.ArticleDomain
import com.example.democompose.data.repository.ArticlesRepository
import eu.anifantakis.mod.coredata.RepositoryResponse
import eu.anifantakis.mod.coredata.network.operations.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface ArticlesManager {
    val articles: StateFlow<List<ArticleDomain>>
    val selectedArticle: State<ArticleDomain?>

    suspend fun queryWithoutCallback(query: String, fromDate: String, sortBy: String)
    suspend fun queryWithCallback(query: String, fromDate: String, sortBy: String): Flow<RepositoryResponse<List<ArticleDomain>>>
    fun getSingleArticle(articleId: String)
    fun clearArticleSelection()
}

class ArticlesManagerImpl @Inject constructor(private val articlesRepository: ArticlesRepository): ArticlesManager {

    private var _articles = MutableStateFlow<List<ArticleDomain>>(emptyList())
    override val articles: StateFlow<List<ArticleDomain>> = _articles

    private val _selectedArticle = mutableStateOf<ArticleDomain?>(null)
    override val selectedArticle: State<ArticleDomain?> = _selectedArticle

    override suspend fun queryWithCallback(query: String, fromDate: String, sortBy: String): Flow<RepositoryResponse<List<ArticleDomain>>> = flow {
        articlesRepository.getDomainArticlesCached(query, fromDate, sortBy).collect { result ->
            when (result) {
                is NetworkResult.Error -> {
                    saveArticlesToStateFlow(articles = result.data)
                    // send back to the caller only when error or success
                    emit(RepositoryResponse.Error(result.message ?: "", result.data as List<ArticleDomain>))
                }
                is NetworkResult.Loading -> {
                    saveArticlesToStateFlow(articles = result.data)
                }
                is NetworkResult.Success -> {
                    saveArticlesToStateFlow(articles = result.data)
                    // emit back to the caller only when error or success
                    emit(RepositoryResponse.Success(result.data as List<ArticleDomain>))
                }
            }
        }
    }

    override suspend fun queryWithoutCallback(query: String, fromDate: String, sortBy: String) {
        articlesRepository.getDomainArticlesCached(query, fromDate, sortBy).collect { result ->
            when (result) {
                is NetworkResult.Error -> {
                    saveArticlesToStateFlow(articles = result.data)
                }
                is NetworkResult.Loading -> {
                    saveArticlesToStateFlow(articles = result.data)
                }
                is NetworkResult.Success -> {
                    saveArticlesToStateFlow(articles = result.data)
                }
            }
        }
    }

    override fun clearArticleSelection() {
        _selectedArticle.value = null
    }

    private fun saveArticlesToStateFlow(articles: List<ArticleDomain>?) {
        articles?.let { _articles.value = articles.ifEmpty { emptyList() } }
    }

    override fun getSingleArticle(articleId: String) {
        _selectedArticle.value = articles.value.firstOrNull { articleDomain ->
            articleDomain.id == articleId
        }
    }
}