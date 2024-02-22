package com.example.democompose.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.democompose.ScaffoldViewModel
import com.example.democompose.views.articles.ArticlesViewModel
import com.example.democompose.views.articles.DetailView
import com.example.democompose.views.articles.MasterView
import com.example.democompose.views.base.BaseViewModel
import com.example.democompose.views.base.MyTopAppBar
import com.example.democompose.views.base.findActivity
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
fun ApplicationScaffold(scaffoldViewModel: ScaffoldViewModel = hiltViewModel(LocalContext.current.findActivity())) {
    val navController = rememberNavController()

    val title by scaffoldViewModel.title.collectAsState()
    val onBackPress by scaffoldViewModel.onBackPress.collectAsState()

    Scaffold(
        topBar = { MyTopAppBar(title, onBackPress) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
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

/**
 * Allow for shared view model within nested navigation
 */
@Composable
inline fun <reified T : BaseViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}