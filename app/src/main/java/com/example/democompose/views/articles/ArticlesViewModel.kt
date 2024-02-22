package com.example.democompose.views.articles

import androidx.lifecycle.viewModelScope
import com.example.democompose.data.model.domain.ArticleDomain
import com.example.democompose.manager.ArticlesManager
import com.example.democompose.views.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.anifantakis.mod.coredata.RepositoryResponse
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val articlesManager: ArticlesManager,
) : BaseViewModel() {
    // this articles item gets updated automatically by the repository when a "query" happens
    val articles: StateFlow<List<ArticleDomain>> = articlesManager.articles

    val selectedArticle = articlesManager.selectedArticle

    fun retrieveArticleWithId(articleId: String) {
        articlesManager.getSingleArticle(articleId)
    }

    // activates via pull to refresh
    fun fetchArticlesPull() {
        val fetchFromDate = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            loadingPull {
                // for demo here I expose "success" and "error". You can remove the "apply" statement if you want
                // since "articles" is exposed directly from the repo, but it is useful to do additional jobs when
                // finished with success or error directly with callback here
                articlesManager.queryWithCallback("Tesla", fetchFromDate, "publishedAt").collectLatest { result ->
                    when (result) {
                        is RepositoryResponse.Error   -> { Timber.e("ERROR: ${result.message}") }
                        is RepositoryResponse.Success -> { Timber.d("SUCCESS - Results: ${result.data?.size ?: 0}") }
                    }
                }
            }
        }
    }

    // the same as above but without a callback.  Here we just trust the manager exposes state-flows that we observe
    // thus not care about grabbing some result
    fun fetchArticlesWithoutCallback() {
        val fetchFromDate = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            loadingPull {
                articlesManager.queryWithoutCallback("Tesla", fetchFromDate, "publishedAt")
            }
        }
    }

    // activates via screen loading
    fun fetchArticles() {
        // we fetch onResume, but don't want to fetch if we just came from a DetailView.
        if (articlesManager.selectedArticle.value != null) {
            articlesManager.clearArticleSelection()
            return
        }

        // now fetch articles
        val fetchFromDate = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            loading {
                // for demo here I expose "success" and "error". You can remove the "apply" statement if you want
                // since "articles" is exposed directly from the repo, but it is useful to do additional jobs when
                // finished with success or error directly with callback here
                articlesManager.queryWithCallback("Tesla", fetchFromDate, "publishedAt").collectLatest { result ->
                    when (result) {
                        is RepositoryResponse.Error   -> { Timber.e("ERROR: ${result.message}") }
                        is RepositoryResponse.Success -> { Timber.d("SUCCESS - Results: ${result.data?.size ?: 0}") }
                    }
                }
            }
        }
    }
}