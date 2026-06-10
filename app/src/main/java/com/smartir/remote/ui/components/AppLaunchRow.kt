package com.smartir.remote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartir.remote.adb.TvApp
import com.smartir.remote.ui.viewmodel.RemoteViewModel

@Composable
fun AppLaunchRow(
    apps: List<TvApp>,
    viewModel: RemoteViewModel,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) return

    val row1 = apps.take(3)
    val row2 = apps.drop(3)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AdaptiveAppRow(apps = row1, viewModel = viewModel, isConnected = isConnected)
        if (row2.isNotEmpty()) {
            AdaptiveAppRow(apps = row2, viewModel = viewModel, isConnected = isConnected)
        }
    }
}

@Composable
private fun AdaptiveAppRow(
    apps: List<TvApp>,
    viewModel: RemoteViewModel,
    isConnected: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            AdbButton(
                onClick = { viewModel.launchTvApp(app) },
                label = app.name,
                enabled = isConnected,
                modifier = Modifier.weight(1f),
                size = 40.dp,
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
