package com.smartir.remote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartir.remote.adb.AdbConnectionState
import com.smartir.remote.ui.theme.AdbConnected
import com.smartir.remote.ui.theme.AdbError
import com.smartir.remote.ui.viewmodel.RemoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbConnectionSheet(
    viewModel: RemoteViewModel,
    adbState: AdbConnectionState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var ipInput by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var discoveredIps by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load last saved IP
    LaunchedEffect(Unit) {
        val lastIp = viewModel.adbManager.preferences.getLastIp()
        if (lastIp != null) {
            ipInput = lastIp
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1A1A2E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ADB Connection",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Status display
            when (adbState) {
                is AdbConnectionState.Connected -> {
                    Text(
                        text = "Connected to ${adbState.ip}",
                        color = AdbConnected,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.disconnectAdb() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AdbError
                        )
                    ) {
                        Icon(
                            Icons.Default.LinkOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnect")
                    }
                }

                is AdbConnectionState.Connecting -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Connecting...",
                            color = Color(0xFFFFC107),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is AdbConnectionState.Error -> {
                    Text(
                        text = adbState.message,
                        color = AdbError,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is AdbConnectionState.Disconnected -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-discover button
            OutlinedButton(
                onClick = {
                    isScanning = true
                    discoveredIps = emptyList()
                    viewModel.discoverTvs { ips ->
                        discoveredIps = ips
                        isScanning = false
                        if (ips.size == 1) {
                            ipInput = ips.first()
                        }
                    }
                },
                enabled = !isScanning && adbState !is AdbConnectionState.Connecting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning network...")
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-discover TVs")
                }
            }

            // Show discovered IPs
            if (discoveredIps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                discoveredIps.forEach { ip ->
                    TextButton(
                        onClick = { ipInput = ip },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(ip)
                    }
                }
            } else if (!isScanning && discoveredIps.isEmpty() && adbState is AdbConnectionState.Disconnected) {
                // Only show "no devices" after a scan completes
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual IP input
            OutlinedTextField(
                value = ipInput,
                onValueChange = { ipInput = it },
                label = { Text("TV IP Address") },
                placeholder = { Text("192.168.1.x") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (ipInput.isNotBlank()) {
                            viewModel.connectAdb(ipInput.trim())
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF444466),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color(0xFF9898A8),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connect button
            Button(
                onClick = { viewModel.connectAdb(ipInput.trim()) },
                enabled = ipInput.isNotBlank() &&
                        adbState !is AdbConnectionState.Connecting &&
                        adbState !is AdbConnectionState.Connected,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Connect")
            }

        }
    }
}
