package com.example.democompose.views.sample

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.democompose.views.base.ExtraPaddings
import com.example.democompose.views.base.LifecycleConfig
import com.example.democompose.views.base.ScreenWithLoadingIndicator
import com.example.democompose.views.base.TopAppBarConfig

/**
 * Simple screen to show initializing hilt viewmodel
 * To show displaying values from state, stateflow, sharedflow
 */
@Composable
fun SampleScreen(
    paddingValues: PaddingValues,
    viewModel: SampleViewModel = hiltViewModel()
) {
    ScreenWithLoadingIndicator(
        topAppBarConfig = TopAppBarConfig(title = "Sample Screen"),
        lifecycleConfig = LifecycleConfig(),
        paddingValues = paddingValues,
        extraPaddings = ExtraPaddings(16.dp)
    ) {
        // Without Encrypted Shared data as property delegation
//        val context = LocalContext.current
//        LaunchedEffect(key1 = Unit) {
//            var count: Int by context.sharedPreferences("count", 0)
//            //var count: String by context.sharedPreferences("count")
//
//            viewModel.stateNum = count
//
//        }

        // With Encrypted Shared data as property delegation
        LaunchedEffect(key1 = Unit) {
            viewModel.stateNum = viewModel.count
        }

        // using state flow in ViewModel you need to collect inside composable
        val stateFlowNum by viewModel.stateFlowNum.collectAsStateWithLifecycle()
        val sharedFlowNum by viewModel.sharedFlowNum.collectAsStateWithLifecycle(0)

        // but to collect stateNum you don't need anything,
        // cause its of state type at the viewmodel

        // State to control when to show the AlertDialog
        // This demonstrates holding a state inside composable and not inside ViewModel
        var showDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // this column will centerHorizontally align the other column and the button
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // this column has the default alignment for text (Start)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    AnimatedContent(
                        targetState = viewModel.stateNum,
                        label = "stateNum",
                        transitionSpec = {
                            //scaleIn(animationSpec = tween(durationMillis = 300), initialScale = 0.8f) togetherWith scaleOut(animationSpec = tween(durationMillis = 300), targetScale = 1.2f)

                            slideInVertically { it } togetherWith slideOutVertically { -it }
                        }
                    ) {
                        Text("stateNum -> ${it}")
                    }

                    Text("stateNum -> ${viewModel.stateNum}")
                    Text("stateFlowNum -> $stateFlowNum")
                    Text("sharedFlowNum -> $sharedFlowNum")

                    Text("persist state 1 -> ${viewModel.persistedNumber1State}")
                    Text("persist state 2 -> ${viewModel.persistedNumber2}")

                    Button(onClick = {
                        viewModel.incrementCounters()
                    }) {
                        Text("Increment Counters")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                , contentAlignment = Alignment.Center
            ) {
                Button(onClick = {
                    showDialog = true
                }) {
                    Text("Display Material3 Alert")
                }

                // Conditionally show the dialog based on the state
                if (showDialog) {
                   ShowAlertDialog(onDismiss = { showDialog = false })
                }
            }
        }
    }
}

@Composable
fun ShowAlertDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            // Call onDismiss when the user tries to dismiss the dialog
            onDismiss()
        },
        title = {
            Text(text = "Alert Dialog Title")
        },
        text = {
            Text("This is an example of triggering an alert dialog from a button click in a composable.")
        },
        confirmButton = {
            Button(
                onClick = {
                    // Handle confirm action here
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    // Handle dismiss action here
                    onDismiss()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}