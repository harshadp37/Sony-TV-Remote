package com.smartir.remote.ui.viewmodel

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartir.remote.haptics.HapticManager
import com.smartir.remote.adb.AdbConnectionManager
import com.smartir.remote.adb.AdbConnectionState
import com.smartir.remote.adb.TvApp
import com.smartir.remote.ir.IrTransmitter
import com.smartir.remote.ir.SonyCodes
import com.smartir.remote.ir.SonyIrCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    val irTransmitter = IrTransmitter(application)
    val adbManager = AdbConnectionManager(application)
    val adbState: StateFlow<AdbConnectionState> = adbManager.state

    private var repeatJob: Job? = null
    private val app = application

    companion object {
        private const val TAG = "SmartIR"
        private const val REPEAT_INTERVAL_MS = 200L
    }

    init {
        Log.d(TAG, "IR blaster available: ${irTransmitter.hasIrEmitter}")
        autoConnect()
    }

    private fun autoConnect() {
        viewModelScope.launch {
            val lastIp = adbManager.preferences.getLastIp()
            if (lastIp != null) {
                Log.d(TAG, "Auto-connecting to last known TV: $lastIp")
                adbManager.connect(lastIp)
            }
        }
    }

    /**
     * Send a single command. Prefers ADB when connected and the command has an
     * adbKeyCode mapping; falls back to IR (3-frame burst) otherwise.
     */
    fun sendCommand(command: SonyIrCommand, view: View) {
        // Cancel any ongoing repeat
        repeatJob?.cancel()
        repeatJob = null

        viewModelScope.launch {
            val useAdb = adbState.value is AdbConnectionState.Connected && command.adbKeyCode != null
            Log.d(TAG, "Sending: ${command.name} via ${if (useAdb) "ADB" else "IR"}")
            try {
                if (useAdb) {
                    adbManager.sendKeyEvent(command.adbKeyCode!!)
                } else {
                    irTransmitter.transmit(command, singleFrame = false)
                }
                withContext(Dispatchers.Main) {
                    if (command == SonyCodes.POWER) {
                        HapticManager.confirm(view)
                    } else {
                        HapticManager.tick(view)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Command failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, "Command error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Start hold-to-repeat: sends key events at ~200ms intervals.
     * Uses ADB keyevent when connected, IR 3-frame bursts otherwise.
     * Cancels any previous repeat job (only one signal at a time).
     */
    fun startRepeat(command: SonyIrCommand, view: View) {
        repeatJob?.cancel()
        repeatJob = viewModelScope.launch {
            // Send initial press immediately
            sendSinglePress(command)
            withContext(Dispatchers.Main) {
                HapticManager.tick(view)
            }

            // Repeat so the TV registers each as a new press
            while (isActive) {
                delay(REPEAT_INTERVAL_MS)
                sendSinglePress(command)
                withContext(Dispatchers.Main) {
                    HapticManager.tick(view)
                }
            }
        }
    }

    /**
     * Send a single key press via ADB (preferred) or IR (fallback).
     */
    private suspend fun sendSinglePress(command: SonyIrCommand) {
        val useAdb = adbState.value is AdbConnectionState.Connected && command.adbKeyCode != null
        if (useAdb) {
            adbManager.sendKeyEvent(command.adbKeyCode!!)
        } else {
            irTransmitter.transmit(command, singleFrame = false)
        }
    }

    /**
     * Stop the current repeat loop.
     */
    fun stopRepeat() {
        repeatJob?.cancel()
        repeatJob = null
    }

    // --- ADB methods ---

    fun connectAdb(ip: String) {
        viewModelScope.launch {
            adbManager.connect(ip)
        }
    }

    fun disconnectAdb() {
        viewModelScope.launch {
            adbManager.disconnect()
        }
    }

    fun launchTvApp(app: TvApp) {
        viewModelScope.launch {
            adbManager.launchApp(app)
        }
    }

    fun sendVoiceText(text: String) {
        viewModelScope.launch {
            adbManager.sendTextInput(text)
        }
    }

    fun discoverTvs(onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val ips = adbManager.discoverTvs()
            withContext(Dispatchers.Main) {
                onResult(ips)
            }
        }
    }
}
