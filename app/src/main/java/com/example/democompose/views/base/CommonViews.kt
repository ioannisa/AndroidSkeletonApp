package com.example.democompose.views.base

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.democompose.views.base.scaffold.ScaffoldViewModel

/**
 * A Circular Progress Indicator that blocks background touches
 * and also blocks "back button" press while loading for critical loading operations.
 */
@Composable
fun LoadingIndicator(isLoading: Boolean, isCritical: Boolean) {
    if (isLoading) {
        if (isCritical) {
            BackHandler(enabled = true) { }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ContentWithLifecycleEvents(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    lifecycleConfig: LifecycleConfig
) {
    val onFirstCreateExecuted = rememberSaveable { mutableStateOf(false) }
    val onCreateExecuted = remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner.lifecycle) {
        val observer = LifecycleEventObserver { source, event ->
            lifecycleConfig.onAny()
            when (event) {
                Lifecycle.Event.ON_RESUME -> lifecycleConfig.onResume()
                Lifecycle.Event.ON_START -> lifecycleConfig.onStart()
                Lifecycle.Event.ON_PAUSE -> lifecycleConfig.onPause()
                Lifecycle.Event.ON_STOP -> lifecycleConfig.onStop()
                Lifecycle.Event.ON_DESTROY -> lifecycleConfig.onDestroy()
                Lifecycle.Event.ON_CREATE -> {
                    // classic onCreate that repeats on orientation change
                    if (!onCreateExecuted.value) {
                        lifecycleConfig.onCreate()
                        onCreateExecuted.value = true
                    }

                    // custom onCreate that executes once regardless of orientation change
                    if (!onFirstCreateExecuted.value) {
                        lifecycleConfig.onFirstCreate()
                        onFirstCreateExecuted.value = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

data class TopAppBarConfig(
    val title: String? = null,
    val onBackPress: (() -> Unit)? = null
)

data class LoadingConfig(
    var isLoading: Boolean = false,
    val criticalContent: Boolean = false
)

data class LifecycleConfig(
    val onResume: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onPause: () -> Unit = {},
    val onStop: () -> Unit = {},
    val onDestroy: () -> Unit = {},
    val onCreate: () -> Unit = {},
    val onFirstCreate: () -> Unit = {},
    val onAny: () -> Unit = {},
)

data class ExtraPaddings(
    val top: Dp = 0.dp,
    val start: Dp = 0.dp,
    val end: Dp = 0.dp,
    val bottom: Dp = 0.dp
) {
    constructor(all: Dp) : this(top = all, start = all, end = all, bottom = all)
    constructor(horizontal: Dp, vertical: Dp) : this(top = vertical, start = horizontal, end = horizontal, bottom = vertical)
}

@Composable
fun ScreenWithLoadingIndicator (
    topAppBarConfig: TopAppBarConfig = TopAppBarConfig(),
    loadingConfig: LoadingConfig = LoadingConfig(),
    lifecycleConfig: LifecycleConfig,

    scaffoldViewModel: ScaffoldViewModel = hiltViewModel(LocalContext.current.findActivity()),
    paddingValues: PaddingValues,
    extraPaddings: ExtraPaddings = ExtraPaddings(),

    content: @Composable () -> Unit
) {
    val topBarHeight by scaffoldViewModel.topBarHeight.collectAsState(0)

    ContentWithLifecycleEvents(
        lifecycleConfig = LifecycleConfig(
            onResume = lifecycleConfig.onResume,
            onStart = lifecycleConfig.onStart,
            onPause = lifecycleConfig.onPause,
            onStop = lifecycleConfig.onStop,
            onDestroy = lifecycleConfig.onDestroy,
            onCreate = lifecycleConfig.onCreate,
            onFirstCreate = lifecycleConfig.onFirstCreate,
            onAny = lifecycleConfig.onAny
        )
    )

    LaunchedEffect(topAppBarConfig) {
        scaffoldViewModel.updateAppBar(
            title = topAppBarConfig.title,
            onBackPress = topAppBarConfig.onBackPress
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(
            top = paddingValues.calculateTopPadding() + extraPaddings.top,
            start = paddingValues.calculateStartPadding(layoutDirection = LocalLayoutDirection.current) + extraPaddings.start,
            end = paddingValues.calculateEndPadding(layoutDirection = LocalLayoutDirection.current) + extraPaddings.end,
            bottom = paddingValues.calculateBottomPadding() + extraPaddings.bottom
        )) {
        Column {
            //MyTopAppBar(title = topAppBarTitle, onBackPress = topAppBarOnBackPress)
            content()
        }
        LoadingIndicator(loadingConfig.isLoading, loadingConfig.criticalContent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(title: String? = null, onBackPress: (() -> Unit)? = null) {

    TopAppBar(
        title = { Text(title ?: "", color = MaterialTheme.colorScheme.onPrimaryContainer) },
        actions = {} ,

        navigationIcon = {
            onBackPress?.let {
                IconButton(onClick = it) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

// https://developer.android.com/reference/kotlin/androidx/compose/material3/pulltorefresh/package-summary
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshList (
    isRefreshing: Boolean,
    onPull: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()
    var started by rememberSaveable { mutableStateOf(false) }

    if (!isRefreshing && started) {
        state.endRefresh()
        started = false
    }

    if (state.isRefreshing) {
        LaunchedEffect(state.isRefreshing) {
            onPull()
            started = true
        }
    }
    val scaleFraction = if (state.isRefreshing) 1f else
        LinearOutSlowInEasing.transform(state.progress).coerceIn(0f, 1f)

    Box(Modifier.nestedScroll(state.nestedScrollConnection)) {
        content()

        PullToRefreshContainer(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer(scaleX = scaleFraction, scaleY = scaleFraction),
            state = state,
        )
    }
}

@Composable
fun NoNetworkDialog(showDialog: Boolean, onContinueOffline: () -> Unit, onQuit: () -> Unit) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("No Network Connection") },
            text = { Text("You are not connected to the internet. Do you want to continue in offline mode or quit the app?") },
            confirmButton = {
                TextButton(onClick = onContinueOffline) {
                    Text("Continue Offline")
                }
            },
            dismissButton = {
                TextButton(onClick = onQuit) {
                    Text("Quit App")
                }
            }
        )
    }
}

// Helper function to find the activity in LocalContext
@Composable
fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) {
            return context
        }
        context = context.baseContext
    }
    throw IllegalStateException("Context does not contain an activity.")
}