package com.example.democompose.views.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.democompose.R
import com.example.democompose.views.base.LifecycleConfig
import com.example.democompose.views.base.ScreenWithLoadingIndicator
import com.example.democompose.views.base.TopAppBarConfig

@Composable
fun DetailView(
    articleId: String,
    viewModel: ArticlesViewModel,
    paddingValues: PaddingValues,
    onNavigateUp: () -> Unit
) {
    ScreenWithLoadingIndicator(
        topAppBarConfig = TopAppBarConfig(
            title = "Article Details",
            onBackPress = { onNavigateUp() }
        ),
        lifecycleConfig = LifecycleConfig(
            onStart = { viewModel.retrieveArticleWithId(articleId) }
        ),
        paddingValues = paddingValues,
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .padding(bottom = paddingValues.calculateBottomPadding())
            .verticalScroll(rememberScrollState())
        ) {
            viewModel.selectedArticle.value?.let { article ->
                article.urlToImage?.let {
                    ImageLoaderDetail(imageUrl = article.urlToImage)
                }

                Text(text = article.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp))
                Text(text = article.content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ImageLoaderDetail(imageUrl: String?, scale: ContentScale = ContentScale.Crop) {
    val imagePainter = if (!imageUrl.isNullOrEmpty()) {
        rememberAsyncImagePainter(
            model = imageUrl,
            filterQuality = FilterQuality.High
        )
    } else {
        painterResource(R.drawable.ic_launcher_foreground)
    }

    AnimatedVisibility(visible = true) {
        Image(
            painter = imagePainter,
            contentDescription = null,
            contentScale = scale,
            modifier = Modifier
                .fillMaxWidth() // Make the image fill the width
                .heightIn(max = 400.dp) // Set the maximum height to 400dp
        )
    }
}