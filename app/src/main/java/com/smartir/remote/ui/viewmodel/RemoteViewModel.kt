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
    }

    /**
     * Send a single command (3-frame burst). Use for tap actions.
     */
    fun sendCommand(command: SonyIrCommand, view: View) {
        // Cancel any ongoing repeat
        repeatJob?.cancel()
        repeatJob = null

        viewModelScope.launch {
            Log.d(TAG, "Sending: ${command.name}")
            try {
                irTransmitter.transmit(command, singleFrame = false)
                withContext(Dispatchers.Main) {
                    if (command == SonyCodes.POWER) {
                        HapticManager.confirm(view)
                    } else {
                        HapticManager.tick(view)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "IR transmit failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, "IR error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Start hold-to-repeat: sends full 3-frame bursts at ~200ms intervals.
     * Each burst is treated as a distinct key press by the TV.
     * Cancels any previous repeat job (only one signal at a time).
     */
    fun startRepeat(command: SonyIrCommand, view: View) {
        repeatJob?.cancel()
        repeatJob = viewModelScope.launch {
            // Send initial burst immediately
            irTransmitter.transmit(command, singleFrame = false)
            withContext(Dispatchers.Main) {
                HapticManager.tick(view)
            }

            // Repeat with full 3-frame bursts so the TV registers each as a new press
            while (isActive) {
                delay(REPEAT_INTERVAL_MS)
                irTransmitter.transmit(command, singleFrame = false)
                withContext(Dispatchers.Main) {
                    HapticManager.tick(view)
                }
            }
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
