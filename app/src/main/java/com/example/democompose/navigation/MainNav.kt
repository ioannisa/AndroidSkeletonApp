package com.example.democompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.democompose.views.articles.ArticlesViewModel
import com.example.democompose.views.articles.DetailView
import com.example.democompose.views.articles.MasterView
import com.example.democompose.views.base.scaffold.ApplicationScaffold
import com.example.democompose.views.base.scaffold.sharedViewModel
import com.example.democompose.views.sample.SampleScreen

// Routes for the nested navigation graphs
sealed class NestedNavGraph(val route: String) {
    data object OnlineArticles: NestedNavGraph("online_articles")
    data object StoredArticles: NestedNavGraph("stored_articles")
}

// Routes for Composables within a navigation graph
sealed class Destination(val route: String) {
    // master/detail screens
    data object Master: Destination("master")
    data object Detail: Destination("detail/{articleId}") {
        fun makeRoute(articleId: String?) = "detail/$articleId"
    }

    // authentication screen / firebase articles screen
    data object FirebaseScreen: Destination("firebase_screen")
}

@Composable
fun ComposeRoot() {
    val navController = rememberNavController()

    ApplicationScaffold(navController = navController) { paddingValues ->
        NavHost(navController = navController, startDestination = "online_articles") {
            navigation(
                startDestination = Destination.Master.route,
                route = NestedNavGraph.OnlineArticles.route
            ) {
                composable(Destination.Master.route) {
                    val viewModel = it.sharedViewModel<ArticlesViewModel>(navController)
                    MasterView(paddingValues = paddingValues, viewModel = viewModel, onNavigateToDetailScreen = navController::navigate )
                }

                composable(Destination.Detail.route) {
                    val articleId = it.arguments?.getString("articleId")
                    val viewModel = it.sharedViewModel<ArticlesViewModel>(navController)

                    articleId?.let {
                        DetailView(articleId = articleId, viewModel = viewModel, paddingValues = paddingValues, onNavigateUp = navController::navigateUp)
                    }
                }
            }

            navigation(
                startDestination = Destination.FirebaseScreen.route,
                route = NestedNavGraph.StoredArticles.route
            ) {
                composable(Destination.FirebaseScreen.route) {
                    SampleScreen(paddingValues)
                }
            }
        }
    }
}