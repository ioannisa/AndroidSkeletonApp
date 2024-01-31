package com.example.democompose.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.democompose.R
import com.example.democompose.data.model.domain.ArticleDomain
import com.example.democompose.navigation.Destination
import com.example.democompose.navigation.NavEvent
import com.example.democompose.views.base.LifecycleConfig
import com.example.democompose.views.base.LoadingConfig
import com.example.democompose.views.base.PullToRefreshList
import com.example.democompose.views.base.ScreenWithLoadingIndicator
import com.example.democompose.views.base.TopAppBarConfig

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MasterView(
    viewModel: ArticlesViewModel,
    paddingValues: PaddingValues,
    onNavigateToDetailScreen: (NavEvent.Navigate) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingPUll by viewModel.isLoadingPull.collectAsState()

    val result by viewModel.articles.collectAsState()

    ScreenWithLoadingIndicator(
        topAppBarConfig = TopAppBarConfig(title = "Tesla Articles"),
        // if set to critical content, blocks back button while loader is spinning (useful for scenarios like spinning during checkout process)
        loadingConfig = LoadingConfig(isLoading, criticalContent = true),
        lifecycleConfig = LifecycleConfig(
            onResume = { viewModel.fetchArticles() },
            onCreate = { },
        ),
        paddingValues = paddingValues
    ) {
        result.let { articles ->
            PullToRefreshList(isLoadingPUll, onPull = { viewModel.fetchArticlesPull() }) {
                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    val groups = articles.groupBy { it.author }

                    groups.forEach { (groupTitle, groupArticles) ->
                        stickyHeader {
                            Text(
                                text = groupTitle ?: "Anonymous",
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.background)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        items(
                            items = groupArticles,
                            key = { it.id }
                        ) { article ->
                            ListItem( {
                                RowItem(
                                    article = article,
                                    modifier = Modifier
                                        .clickable {
                                            onNavigateToDetailScreen(NavEvent.Navigate(Destination.Detail.makeRoute(article.id)))
                                        }
                                        .background(MaterialTheme.colorScheme.surface)
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha =
                                                if (viewModel.selectedArticle.value == article) 0f else 1f
                                        }
                                )
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowItem(article: ArticleDomain, modifier: Modifier) {
    Card(modifier = modifier) {
        Row() {
            ImageLoader(imageUrl = article.urlToImage)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ImageLoader(imageUrl: String?, scale: ContentScale = ContentScale.Crop, size: Int = 120) {
    val imagePainter = if (!imageUrl.isNullOrEmpty()) {
        rememberAsyncImagePainter(
            model = imageUrl,
            filterQuality = FilterQuality.Low
        )
    } else {
        painterResource(R.drawable.ic_launcher_foreground)
    }

    Image(
        painter = imagePainter,
        contentDescription = null,
        contentScale = scale,
        modifier = Modifier.size(size.dp)
    )
}
