package com.example.democompose

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.democompose.navigation.ApplicationScaffold
import com.example.democompose.ui.theme.DemoComposeTheme
import com.example.democompose.utils.ObservableLoadingInteger
import com.example.democompose.views.base.NoNetworkDialog
import dagger.hilt.android.AndroidEntryPoint
import eu.anifantakis.mod.coredata.network.monitor.ConnectivityMonitor
import eu.anifantakis.mod.coredata.network.monitor.ConnectivityObservable
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var connectivityMonitor: ConnectivityMonitor
    @Inject lateinit var loadingCounter: ObservableLoadingInteger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                loadingCounter.value.collect { count ->
                    // Enable or disable rotation based on the count
                    if (count > 0) {
                        lockScreenOrientation()
                    } else {
                        unlockScreenOrientation()
                    }
                }
            }
        }

        setContent {
            DemoComposeTheme {
                val networkAvailable = connectivityMonitor.observe().collectAsState(
                    ConnectivityObservable.Status.Available)

                // State to control the visibility of the dialog
                var showDialog by rememberSaveable { mutableStateOf(false) }

                // Check network availability and show dialog
                LaunchedEffect(networkAvailable.value) {
                    showDialog = networkAvailable.value == ConnectivityObservable.Status.Unavailable
                }

                NoNetworkDialog(
                    showDialog,
                    onContinueOffline = { showDialog = false },
                    onQuit = { this.finish() })

                ApplicationScaffold()
            }
        }
    }

    private fun lockScreenOrientation() {
        Timber.d("SCREEN ORIENTATION - LOCKED")
        val currentOrientation = resources.configuration.orientation
        requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        }
    }

    private fun unlockScreenOrientation() {
        Timber.d("SCREEN ORIENTATION - UNLOCKED")
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    }
}