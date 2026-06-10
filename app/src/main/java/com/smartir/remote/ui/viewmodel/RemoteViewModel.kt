package com.smartir.remote.ui.viewmodel

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.smartir.remote.haptics.HapticManager
import com.smartir.remote.adb.AdbConnectionManager
import com.smartir.remote.adb.AdbConnectionState
import com.smartir.remote.adb.TvApp
import com.smartir.remote.adb.TvApps
import com.smartir.remote.data.AppPreferences
import com.smartir.remote.data.CustomApp
import com.smartir.remote.data.toTvApp
import com.smartir.remote.ir.IrTransmitter
import com.smartir.remote.ir.SonyCodes
import com.smartir.remote.ir.SonyIrCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Defines an acceleration stage for hold-to-repeat.
 * @param thresholdMs elapsed hold time (ms) at which this stage activates
 * @param intervalMs delay between ticks at this stage
 * @param burstCount number of key events sent per tick
 */
data class AccelStage(val thresholdMs: Long, val intervalMs: Long, val burstCount: Int)

/**
 * Acceleration profiles for different button types.
 * Stages must be ordered by thresholdMs ascending.
 */
object RepeatProfiles {
    /** Volume: ramps up to fast single-step, then adds bursts for big jumps */
    val VOLUME = listOf(
        AccelStage(thresholdMs = 0, intervalMs = 200, burstCount = 1),
        AccelStage(thresholdMs = 800, intervalMs = 100, burstCount = 1),
        AccelStage(thresholdMs = 1500, intervalMs = 60, burstCount = 1),
        AccelStage(thresholdMs = 2500, intervalMs = 50, burstCount = 2)
    )

    /** D-Pad: conservative first (menus), aggressive later (video seeking) */
    val DPAD = listOf(
        AccelStage(thresholdMs = 0, intervalMs = 200, burstCount = 1),
        AccelStage(thresholdMs = 1000, intervalMs = 120, burstCount = 1),
        AccelStage(thresholdMs = 2000, intervalMs = 80, burstCount = 2),
        AccelStage(thresholdMs = 3000, intervalMs = 60, burstCount = 3)
    )

    /** Rewind/Fast Forward: aggressive from the start — only used in playback */
    val MEDIA_SEEK = listOf(
        AccelStage(thresholdMs = 0, intervalMs = 180, burstCount = 1),
        AccelStage(thresholdMs = 500, intervalMs = 100, burstCount = 2),
        AccelStage(thresholdMs = 1500, intervalMs = 60, burstCount = 3),
        AccelStage(thresholdMs = 3000, intervalMs = 50, burstCount = 4)
    )
}

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    val irTransmitter = IrTransmitter(application)
    val adbManager = AdbConnectionManager(application)
    val adbState: StateFlow<AdbConnectionState> = adbManager.state
    val appPreferences = AppPreferences(application)

    val selectedApps: StateFlow<List<TvApp>> = combine(
        appPreferences.selectedAppIdsFlow(),
        appPreferences.customAppsFlow()
    ) { ids, customApps ->
        val customMap = customApps.associateBy { it.id }
        ids.mapNotNull { id ->
            TvApps.findById(id) ?: customMap[id]?.toTvApp()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val customApps: StateFlow<List<CustomApp>> = appPreferences.customAppsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var repeatJob: Job? = null
    private val app = application

    companion object {
        private const val TAG = "SmartIR"
    }

    /** Pick the acceleration profile based on the command being repeated. */
    private fun profileFor(command: SonyIrCommand): List<AccelStage> = when (command) {
        SonyCodes.VOLUME_UP, SonyCodes.VOLUME_DOWN -> RepeatProfiles.VOLUME
        SonyCodes.REWIND, SonyCodes.FAST_FORWARD -> RepeatProfiles.MEDIA_SEEK
        else -> RepeatProfiles.DPAD
    }

    /** Returns the active stage for the given elapsed hold time. */
    private fun currentStage(profile: List<AccelStage>, elapsedMs: Long): AccelStage {
        return profile.last { it.thresholdMs <= elapsedMs }
    }

    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            checkAndReconnect()
        }
    }

    init {
        Log.d(TAG, "IR blaster available: ${irTransmitter.hasIrEmitter}")
        autoConnect()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
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
     * Called when the app comes back to the foreground.
     * If we were previously connected, check if the connection is still alive
     * and silently reconnect if it has gone stale.
     */
    private fun checkAndReconnect() {
        val currentState = adbState.value
        val ip = when (currentState) {
            is AdbConnectionState.Connected -> currentState.ip
            is AdbConnectionState.Error -> null
            else -> return
        }

        viewModelScope.launch {
            // For Error state, try the last saved IP
            val targetIp = ip ?: adbManager.preferences.getLastIp() ?: return@launch

            if (currentState is AdbConnectionState.Connected) {
                if (adbManager.isConnectionAlive()) {
                    Log.d(TAG, "Connection still alive after resume")
                    return@launch
                }
                Log.d(TAG, "Connection stale after resume, reconnecting to $targetIp")
            } else {
                Log.d(TAG, "Reconnecting after error to $targetIp")
            }

            adbManager.connect(targetIp)
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
     * Start hold-to-repeat with acceleration.
     * The longer you hold, the faster events are sent (and in bursts for seeking).
     * Uses ADB keyevent when connected, IR 3-frame bursts otherwise.
     * Cancels any previous repeat job (only one signal at a time).
     */
    fun startRepeat(command: SonyIrCommand, view: View) {
        repeatJob?.cancel()
        repeatJob = viewModelScope.launch {
            val profile = profileFor(command)
            val startTime = System.currentTimeMillis()

            // Send initial press immediately
            sendSinglePress(command)
            withContext(Dispatchers.Main) {
                HapticManager.tick(view)
            }

            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val stage = currentStage(profile, elapsed)

                delay(stage.intervalMs)

                // Send burst of events for this stage
                repeat(stage.burstCount) {
                    if (!isActive) return@repeat
                    sendSinglePress(command)
                }
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

    // --- App shortcut management ---

    fun saveSelectedAppIds(ids: List<String>) {
        viewModelScope.launch {
            appPreferences.saveSelectedAppIds(ids)
        }
    }

    fun addCustomApp(name: String, packageName: String, colorHex: Long) {
        viewModelScope.launch {
            val id = "custom_${packageName.replace('.', '_')}"
            val current = customApps.value.toMutableList()
            if (current.none { it.id == id }) {
                current.add(CustomApp(id = id, name = name, packageName = packageName, colorHex = colorHex))
                appPreferences.saveCustomApps(current)
            }
        }
    }

    fun removeCustomApp(customApp: CustomApp) {
        viewModelScope.launch {
            val updatedCustom = customApps.value.filter { it.id != customApp.id }
            appPreferences.saveCustomApps(updatedCustom)
            // Also remove from selected if present
            val updatedIds = selectedApps.value.map { it.id }.filter { it != customApp.id }
            appPreferences.saveSelectedAppIds(updatedIds)
        }
    }
}
