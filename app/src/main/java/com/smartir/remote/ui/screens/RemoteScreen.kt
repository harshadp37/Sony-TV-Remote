package com.smartir.remote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Mic
import com.smartir.remote.adb.AdbConnectionState
import com.smartir.remote.ir.SonyCodes
import com.smartir.remote.ui.components.AdbButton
import com.smartir.remote.ui.components.AdbConnectionSheet
import com.smartir.remote.ui.components.AppLaunchRow
import com.smartir.remote.ui.theme.MicPurple
import com.smartir.remote.ui.components.SegmentedDPad
import com.smartir.remote.ui.components.RemoteButton
import com.smartir.remote.ui.components.SwipeGestureOverlay
import com.smartir.remote.ui.theme.AdbConnected
import com.smartir.remote.ui.theme.AdbConnecting
import com.smartir.remote.ui.theme.AdbDisconnected
import com.smartir.remote.ui.theme.AdbError
import com.smartir.remote.ui.theme.MuteAmber
import com.smartir.remote.ui.theme.PowerRed
import com.smartir.remote.ui.viewmodel.RemoteViewModel

private val NavButtonColor = Color(0xFF2C2C44)
private val NavButtonPressed = Color(0xFF3C3C5C)

@Composable
fun RemoteScreen(
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var showConnectionSheet by remember { mutableStateOf(false) }
    val adbState by viewModel.adbState.collectAsState()
    val isAdbConnected = adbState is AdbConnectionState.Connected

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.sendVoiceText(spokenText)
            }
        }
    }

    if (showConnectionSheet) {
        AdbConnectionSheet(
            viewModel = viewModel,
            adbState = adbState,
            onDismiss = { showConnectionSheet = false }
        )
    }

    SwipeGestureOverlay(viewModel = viewModel) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ADB status indicator chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E30))
                    .clickable { showConnectionSheet = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val (statusColor, statusText) = when (adbState) {
                    is AdbConnectionState.Connected -> AdbConnected to "ADB Connected"
                    is AdbConnectionState.Connecting -> AdbConnecting to "Connecting..."
                    is AdbConnectionState.Error -> AdbError to "ADB Error"
                    is AdbConnectionState.Disconnected -> AdbDisconnected to "ADB Off"
                }
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Power, Input, Menu, Home row
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteButton(
                    command = SonyCodes.POWER,
                    viewModel = viewModel,
                    icon = Icons.Default.PowerSettingsNew,
                    holdToRepeat = false,
                    size = 62.dp,
                    backgroundColor = PowerRed,
                    pressedColor = PowerRed.copy(alpha = 0.7f),
                    contentColor = Color.White,
                    shadowColor = PowerRed,
                    elevation = 14.dp,
                    iconSize = 30.dp
                )

                RemoteButton(
                    command = SonyCodes.INPUT,
                    viewModel = viewModel,
                    icon = Icons.AutoMirrored.Filled.Input,
                    holdToRepeat = false,
                    size = 62.dp,
                    backgroundColor = NavButtonColor,
                    pressedColor = NavButtonPressed,
                    contentColor = Color(0xFFD0D0E8),
                    elevation = 10.dp,
                    iconSize = 30.dp
                )

                RemoteButton(
                    command = SonyCodes.MENU,
                    viewModel = viewModel,
                    icon = Icons.Default.Menu,
                    holdToRepeat = false,
                    size = 62.dp,
                    backgroundColor = NavButtonColor,
                    pressedColor = NavButtonPressed,
                    contentColor = Color(0xFFD0D0E8),
                    elevation = 10.dp,
                    iconSize = 30.dp
                )

                RemoteButton(
                    command = SonyCodes.HOME,
                    viewModel = viewModel,
                    icon = Icons.Default.Home,
                    holdToRepeat = false,
                    size = 62.dp,
                    backgroundColor = NavButtonColor,
                    pressedColor = NavButtonPressed,
                    contentColor = Color(0xFFD0D0E8),
                    elevation = 10.dp,
                    iconSize = 30.dp
                )

                AdbButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search on TV")
                        }
                        speechLauncher.launch(intent)
                    },
                    icon = Icons.Default.Mic,
                    enabled = isAdbConnected,
                    size = 62.dp,
                    backgroundColor = MicPurple.copy(alpha = 0.85f),
                    pressedColor = MicPurple.copy(alpha = 0.6f),
                    contentColor = Color.White,
                    shadowColor = MicPurple,
                    elevation = 12.dp,
                    iconSize = 30.dp,
                    contentDescription = "Voice search"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // App launch row + Mic
            AppLaunchRow(
                viewModel = viewModel,
                isConnected = isAdbConnected
            )

            Spacer(modifier = Modifier.weight(1f))

            // D-Pad (Joystick style)
            SegmentedDPad(viewModel = viewModel)

            Spacer(modifier = Modifier.height(28.dp))

            // Mute (left) + 3-row grid (right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Mute at far left
                RemoteButton(
                    command = SonyCodes.MUTE,
                    viewModel = viewModel,
                    icon = Icons.AutoMirrored.Filled.VolumeOff,
                    holdToRepeat = false,
                    size = 56.dp,
                    backgroundColor = MuteAmber,
                    pressedColor = MuteAmber.copy(alpha = 0.7f),
                    contentColor = Color.White,
                    shadowColor = MuteAmber,
                    elevation = 12.dp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Right side: 3 rows aligned to end
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Vol+
                    RemoteButton(
                        command = SonyCodes.VOLUME_UP,
                        viewModel = viewModel,
                        icon = Icons.Default.Add,
                        holdToRepeat = true,
                        size = 48.dp,
                        backgroundColor = NavButtonColor,
                        pressedColor = NavButtonPressed,
                        contentColor = Color(0xFFD0D0E8),
                        elevation = 10.dp
                    )

                    // Row 2: Back, Vol-
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RemoteButton(
                            command = SonyCodes.BACK,
                            viewModel = viewModel,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            holdToRepeat = false,
                            size = 48.dp,
                            backgroundColor = NavButtonColor,
                            pressedColor = NavButtonPressed,
                            contentColor = Color(0xFFD0D0E8),
                            elevation = 10.dp,
                            iconSize = 24.dp
                        )

                        RemoteButton(
                            command = SonyCodes.VOLUME_DOWN,
                            viewModel = viewModel,
                            icon = Icons.Default.Remove,
                            holdToRepeat = true,
                            size = 48.dp,
                            backgroundColor = NavButtonColor,
                            pressedColor = NavButtonPressed,
                            contentColor = Color(0xFFD0D0E8),
                            elevation = 10.dp
                        )
                    }

                    // Row 3: Play, Rewind, Forward
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RemoteButton(
                            command = if (isPlaying) SonyCodes.PAUSE else SonyCodes.PLAY,
                            viewModel = viewModel,
                            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            holdToRepeat = false,
                            size = 48.dp,
                            backgroundColor = NavButtonColor,
                            pressedColor = NavButtonPressed,
                            contentColor = Color(0xFFD0D0E8),
                            elevation = 10.dp,
                            onSent = { isPlaying = !isPlaying }
                        )

                        RemoteButton(
                            command = SonyCodes.REWIND,
                            viewModel = viewModel,
                            icon = Icons.Default.FastRewind,
                            holdToRepeat = true,
                            size = 48.dp,
                            backgroundColor = NavButtonColor,
                            pressedColor = NavButtonPressed,
                            contentColor = Color(0xFFD0D0E8),
                            elevation = 10.dp
                        )

                        RemoteButton(
                            command = SonyCodes.FAST_FORWARD,
                            viewModel = viewModel,
                            icon = Icons.Default.FastForward,
                            holdToRepeat = true,
                            size = 48.dp,
                            backgroundColor = NavButtonColor,
                            pressedColor = NavButtonPressed,
                            contentColor = Color(0xFFD0D0E8),
                            elevation = 10.dp
                        )
                    }
                }
            }
        }
    }
}
