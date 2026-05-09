package com.smartir.remote.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wrapper around Android's ConsumerIrManager for transmitting Sony SIRC IR signals.
 */
class IrTransmitter(context: Context) {

    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    /** Whether this device has an IR emitter. */
    val hasIrEmitter: Boolean
        get() = irManager?.hasIrEmitter() == true

    /**
     * Transmit an IR command.
     *
     * @param command The Sony IR command to send
     * @param singleFrame If true, sends only 1 frame (for hold-to-repeat).
     *                    If false, sends 3 frames (standard Sony tap).
     */
    suspend fun transmit(command: SonyIrCommand, singleFrame: Boolean = false) {
        val manager = irManager ?: return
        val frameCount = if (singleFrame) 1 else 3
        val pattern = SircEncoder.encode(command, frameCount)

        withContext(Dispatchers.IO) {
            manager.transmit(SircEncoder.CARRIER_FREQUENCY, pattern)
        }
    }
}
