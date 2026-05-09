package com.smartir.remote.ir

/**
 * Encodes Sony SIRC IR commands into mark/space timing patterns
 * suitable for Android's ConsumerIrManager.transmit().
 *
 * Sony SIRC protocol:
 * - Carrier frequency: 40 kHz
 * - Header: 2400µs mark, 600µs space
 * - Logic 1: 1200µs mark, 600µs space
 * - Logic 0: 600µs mark, 600µs space
 * - Bit ordering: LSB first
 * - Frame gap: 45ms total frame time (pad remaining time as space)
 */
object SircEncoder {

    const val CARRIER_FREQUENCY = 40_000 // 40 kHz

    // Timing constants in microseconds
    private const val HEADER_MARK = 2400
    private const val HEADER_SPACE = 600
    private const val ONE_MARK = 1200
    private const val ZERO_MARK = 600
    private const val BIT_SPACE = 600
    private const val FRAME_PERIOD_US = 45_000 // 45ms total frame time

    /**
     * Encode a Sony IR command into an IntArray of alternating mark/space
     * durations in microseconds.
     *
     * @param irCommand The Sony IR command to encode
     * @param frameCount Number of times to repeat the frame (Sony standard: 3)
     * @return IntArray of alternating mark/space timings
     */
    fun encode(irCommand: SonyIrCommand, frameCount: Int = 3): IntArray {
        val singleFrame = encodeSingleFrame(irCommand)
        if (frameCount <= 1) return singleFrame

        val frames = mutableListOf<Int>()
        for (i in 0 until frameCount) {
            frames.addAll(singleFrame.toList())
            if (i < frameCount - 1) {
                // Calculate frame gap: 45ms total frame time minus the transmission time
                val transmissionTime = singleFrame.sum()
                val gap = FRAME_PERIOD_US - transmissionTime
                if (gap > 0) {
                    // Extend the last space of the frame to fill the gap
                    frames[frames.size - 1] = frames[frames.size - 1] + gap
                }
            }
        }
        return frames.toIntArray()
    }

    /**
     * Encode a single SIRC frame (no repetition).
     */
    private fun encodeSingleFrame(irCommand: SonyIrCommand): IntArray {
        val timings = mutableListOf<Int>()

        // Header
        timings.add(HEADER_MARK)
        timings.add(HEADER_SPACE)

        // Command bits (7 bits, LSB first)
        encodeBits(irCommand.command, irCommand.protocol.commandBits, timings)

        // Address bits (5 or 8 bits, LSB first)
        encodeBits(irCommand.address, irCommand.protocol.addressBits, timings)

        // Extended bits for 20-bit protocol (8 bits, LSB first)
        if (irCommand.protocol.extendedBits > 0) {
            encodeBits(irCommand.extended, irCommand.protocol.extendedBits, timings)
        }

        return timings.toIntArray()
    }

    /**
     * Encode N bits of a value LSB-first into mark/space pairs.
     */
    private fun encodeBits(value: Int, bitCount: Int, timings: MutableList<Int>) {
        for (i in 0 until bitCount) {
            val bit = (value shr i) and 1
            if (bit == 1) {
                timings.add(ONE_MARK)
            } else {
                timings.add(ZERO_MARK)
            }
            timings.add(BIT_SPACE)
        }
    }
}
