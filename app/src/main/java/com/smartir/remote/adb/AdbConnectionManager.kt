package com.smartir.remote.adb

import android.content.Context
import android.util.Log
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class AdbConnectionState {
    data object Disconnected : AdbConnectionState()
    data object Connecting : AdbConnectionState()
    data class Connected(val ip: String) : AdbConnectionState()
    data class Error(val message: String) : AdbConnectionState()
}

/**
 * Manages ADB over WiFi connections to the TV.
 * Provides connect/disconnect, app launch, and text input capabilities.
 */
class AdbConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "AdbConnection"
        private const val ADB_PORT = 5555
    }

    private val _state = MutableStateFlow<AdbConnectionState>(AdbConnectionState.Disconnected)
    val state: StateFlow<AdbConnectionState> = _state.asStateFlow()

    private var dadb: Dadb? = null
    private val keyPair: AdbKeyPair by lazy { AdbKeyStore.getOrCreateKeyPair(context) }
    val preferences = AdbPreferences(context)

    suspend fun connect(ip: String) {
        if (_state.value is AdbConnectionState.Connected) {
            disconnect()
        }

        _state.value = AdbConnectionState.Connecting

        withContext(Dispatchers.IO) {
            try {
                val connection = Dadb.create(ip, ADB_PORT, keyPair)
                // Test the connection with a simple command
                connection.shell("echo connected")
                dadb = connection
                _state.value = AdbConnectionState.Connected(ip)
                preferences.saveLastIp(ip)
                Log.d(TAG, "Connected to $ip")
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed: ${e.message}", e)
                _state.value = AdbConnectionState.Error(e.message ?: "Connection failed")
                dadb = null
            }
        }
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                dadb?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing connection: ${e.message}")
            }
            dadb = null
            _state.value = AdbConnectionState.Disconnected
        }
    }

    suspend fun launchApp(app: TvApp): Boolean {
        val connection = dadb ?: return false
        return withContext(Dispatchers.IO) {
            try {
                // Try default LAUNCHER category first
                val response = connection.shell("monkey -p ${app.packageName} 1")
                if (response.exitCode == 0 && !response.allOutput.contains("No activities found")) {
                    Log.d(TAG, "Launch ${app.name}: success")
                    return@withContext true
                }

                // Fallback: TV apps may only register under LEANBACK_LAUNCHER
                val leanback = connection.shell(
                    "monkey -p ${app.packageName} -c android.intent.category.LEANBACK_LAUNCHER 1"
                )
                if (leanback.exitCode == 0 && !leanback.allOutput.contains("No activities found")) {
                    Log.d(TAG, "Launch ${app.name}: success (leanback)")
                    return@withContext true
                }

                Log.e(TAG, "Launch ${app.name}: no launchable activity found")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Launch ${app.name} failed: ${e.message}", e)
                handleConnectionError(e)
                false
            }
        }
    }

    suspend fun sendTextInput(text: String): Boolean {
        val connection = dadb ?: return false
        return withContext(Dispatchers.IO) {
            try {
                // Open search bar
                connection.shell("input keyevent KEYCODE_SEARCH")
                // Small delay for search bar to open
                kotlinx.coroutines.delay(500)
                // Send text with spaces encoded as %s
                val encoded = text.replace(" ", "%s")
                connection.shell("input text '$encoded'")
                Log.d(TAG, "Sent text input: $text")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Text input failed: ${e.message}", e)
                handleConnectionError(e)
                false
            }
        }
    }

    suspend fun sendKeyEvent(keyCode: String): Boolean {
        val connection = dadb ?: return false
        return withContext(Dispatchers.IO) {
            try {
                connection.shell("input keyevent $keyCode")
                Log.d(TAG, "ADB keyevent: $keyCode")
                true
            } catch (e: Exception) {
                Log.e(TAG, "ADB keyevent failed: ${e.message}", e)
                handleConnectionError(e)
                false
            }
        }
    }

    /**
     * Checks if the current connection is still alive by sending a test command.
     * Returns true if healthy, false if dead/disconnected.
     */
    suspend fun isConnectionAlive(): Boolean {
        val connection = dadb ?: return false
        return withContext(Dispatchers.IO) {
            try {
                connection.shell("echo ping")
                true
            } catch (e: Exception) {
                Log.w(TAG, "Connection health check failed: ${e.message}")
                false
            }
        }
    }

    suspend fun discoverTvs(): List<String> {
        return SubnetScanner.scan(context)
    }

    private fun handleConnectionError(e: Exception) {
        _state.value = AdbConnectionState.Error(e.message ?: "Connection lost")
        try {
            dadb?.close()
        } catch (_: Exception) { }
        dadb = null
    }
}
