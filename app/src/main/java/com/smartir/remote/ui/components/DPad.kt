package com.smartir.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartir.remote.ir.SonyCodes
import com.smartir.remote.ui.theme.OkBlue
import com.smartir.remote.ui.viewmodel.RemoteViewModel

private val DpadBackground = Color(0xFF1E1E30)
private val DpadButtonColor = Color(0xFF2A2A40)
private val DpadButtonPressed = Color(0xFF3A3A58)

@Composable
fun DPad(
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(260.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(DpadBackground),
        contentAlignment = Alignment.Center
    ) {
        // Up (wide rectangle)
        RemoteButton(
            command = SonyCodes.DPAD_UP,
            viewModel = viewModel,
            icon = Icons.Default.KeyboardArrowUp,
            holdToRepeat = true,
            width = 100.dp,
            height = 68.dp,
            backgroundColor = DpadButtonColor,
            pressedColor = DpadButtonPressed,
            iconSize = 32.dp,
            elevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        )

        // Down (wide rectangle)
        RemoteButton(
            command = SonyCodes.DPAD_DOWN,
            viewModel = viewModel,
            icon = Icons.Default.KeyboardArrowDown,
            holdToRepeat = true,
            width = 100.dp,
            height = 68.dp,
            backgroundColor = DpadButtonColor,
            pressedColor = DpadButtonPressed,
            iconSize = 32.dp,
            elevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )

        // Left (tall rectangle)
        RemoteButton(
            command = SonyCodes.DPAD_LEFT,
            viewModel = viewModel,
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            holdToRepeat = true,
            width = 68.dp,
            height = 100.dp,
            backgroundColor = DpadButtonColor,
            pressedColor = DpadButtonPressed,
            iconSize = 32.dp,
            elevation = 6.dp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
        )

        // Right (tall rectangle)
        RemoteButton(
            command = SonyCodes.DPAD_RIGHT,
            viewModel = viewModel,
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            holdToRepeat = true,
            width = 68.dp,
            height = 100.dp,
            backgroundColor = DpadButtonColor,
            pressedColor = DpadButtonPressed,
            iconSize = 32.dp,
            elevation = 6.dp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
        )

        // OK (center, circular)
        RemoteButton(
            command = SonyCodes.DPAD_OK,
            viewModel = viewModel,
            label = "OK",
            holdToRepeat = false,
            size = 85.dp,
            backgroundColor = OkBlue,
            pressedColor = OkBlue.copy(alpha = 0.7f),
            contentColor = Color.White,
            shadowColor = OkBlue,
            elevation = 12.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .clip(CircleShape)
        )
    }
}
