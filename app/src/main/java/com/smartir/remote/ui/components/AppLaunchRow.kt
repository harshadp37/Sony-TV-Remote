package com.smartir.remote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartir.remote.adb.TvApps
import com.smartir.remote.adb.TvApp
import com.smartir.remote.ui.viewmodel.RemoteViewModel

@Composable
fun AppLaunchRow(
    viewModel: RemoteViewModel,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppRow(apps = TvApps.ROW_1, viewModel = viewModel, isConnected = isConnected)
        AppRow(apps = TvApps.ROW_2, viewModel = viewModel, isConnected = isConnected)
    }
}

@Composable
private fun AppRow(
    apps: List<TvApp>,
    viewModel: RemoteViewModel,
    isConnected: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            AdbButton(
                onClick = { viewModel.launchTvApp(app) },
                label = app.name,
                enabled = isConnected,
                size = 40.dp,
                width = 96.dp,
                backgroundColor = app.color,
                gradientEndColor = app.colorEnd,
                pressedColor = app.color.copy(alpha = 0.6f),
                contentColor = Color.White,
                shadowColor = app.color,
                elevation = 8.dp
            )
        }
    }
}
