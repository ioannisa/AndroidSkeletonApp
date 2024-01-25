package com.example.democompose.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.democompose.ScaffoldViewModel
import com.example.democompose.views.ArticlesViewModel
import com.example.democompose.views.DetailView
import com.example.democompose.views.MasterView
import com.example.democompose.views.SampleViewModel
import com.example.democompose.views.base.BaseViewModel
import com.example.democompose.views.base.MyTopAppBar
import com.example.democompose.views.base.findActivity

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
                    MasterView(navController = navController, paddingValues = paddingValues, viewModel = viewModel)
                }

                composable(Destination.Detail.route) {
                    val articleId = it.arguments?.getString("articleId")
                    val viewModel = it.sharedViewModel<ArticlesViewModel>(navController)

                    articleId?.let {
                        DetailView(articleId = articleId, navController = navController, viewModel = viewModel, paddingValues = paddingValues)
                    }
                }
            }

            navigation(
                startDestination = Destination.FirebaseScreen.route,
                route = NestedNavGraph.StoredArticles.route
            ) {
                composable(Destination.FirebaseScreen.route) {
                    FirebaseScreen(paddingValues)
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


@Composable
fun FirebaseScreen(
    paddingValues: PaddingValues,
    viewModel: SampleViewModel = hiltViewModel()
) {
    Text("Hello Firebase ${viewModel.selectedNumber.value}",
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding()
            )
    )
}