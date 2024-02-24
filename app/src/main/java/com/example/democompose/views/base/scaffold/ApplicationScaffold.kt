package com.example.democompose.views.base.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.democompose.navigation.BottomNav
import com.example.democompose.views.base.BaseViewModel
import com.example.democompose.views.base.MyTopAppBar
import com.example.democompose.views.base.findActivity

@Composable
fun ApplicationScaffold(
    scaffoldViewModel: ScaffoldViewModel = hiltViewModel(LocalContext.current.findActivity()),
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {
    val title by scaffoldViewModel.title.collectAsState()
    val onBackPress by scaffoldViewModel.onBackPress.collectAsState()

    Scaffold(
        topBar = { MyTopAppBar(title, onBackPress) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        content(paddingValues)
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