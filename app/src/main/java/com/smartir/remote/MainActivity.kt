package com.smartir.remote

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartir.remote.ui.screens.AppShortcutsScreen
import com.smartir.remote.ui.screens.RemoteScreen
import com.smartir.remote.ui.screens.SettingsScreen
import com.smartir.remote.ui.theme.SmartIRRemoteTheme
import com.smartir.remote.ui.viewmodel.RemoteViewModel

sealed class Screen {
    data object Remote : Screen()
    data object Settings : Screen()
    data object AppShortcuts : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            SmartIRRemoteTheme {
                val viewModel: RemoteViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Remote) }

                // Check for IR blaster on launch
                LaunchedEffect(Unit) {
                    if (!viewModel.irTransmitter.hasIrEmitter) {
                        snackbarHostState.showSnackbar(
                            message = "No IR blaster detected on this device"
                        )
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(snackbarData = data)
                        }
                    }
                ) { innerPadding ->
                    when (currentScreen) {
                        Screen.Remote -> RemoteScreen(
                            viewModel = viewModel,
                            onOpenSettings = { currentScreen = Screen.Settings },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.Settings -> SettingsScreen(
                            onClose = { currentScreen = Screen.Remote },
                            onOpenAppShortcuts = { currentScreen = Screen.AppShortcuts },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.AppShortcuts -> AppShortcutsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Settings },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
