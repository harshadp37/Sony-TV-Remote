package com.smartir.remote.ir

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SircEncoderTest {

    @Test
    fun `12-bit command produces correct number of timing values`() {
        // 12-bit: header (2 values) + 12 bits * 2 values each = 26 values
        val pattern = SircEncoder.encode(SonyCodes.POWER, frameCount = 1)
        assertEquals(26, pattern.size)
    }

    @Test
    fun `15-bit command produces correct number of timing values`() {
        // 15-bit: header (2 values) + 15 bits * 2 values each = 32 values
        val pattern = SircEncoder.encode(SonyCodes.DPAD_UP, frameCount = 1)
        assertEquals(32, pattern.size)
    }

    @Test
    fun `20-bit command produces correct number of timing values`() {
        // 20-bit: header (2 values) + 20 bits * 2 values each = 42 values
        val pattern = SircEncoder.encode(SonyCodes.HDMI_1, frameCount = 1)
        assertEquals(42, pattern.size)
    }

    @Test
    fun `header timing is correct`() {
        val pattern = SircEncoder.encode(SonyCodes.POWER, frameCount = 1)
        assertEquals(2400, pattern[0]) // Header mark
        assertEquals(600, pattern[1])  // Header space
    }

    @Test
    fun `all timing values are positive`() {
        SonyCodes.ALL_COMMANDS.forEach { command ->
            val pattern = SircEncoder.encode(command, frameCount = 1)
            pattern.forEachIndexed { index, value ->
                assertTrue(
                    "Timing value at index $index for ${command.name} should be positive, was $value",
                    value > 0
                )
            }
        }
    }

    @Test
    fun `LSB first encoding for power command`() {
        // Power command = 21 (binary: 0010101), address = 1 (binary: 00001)
        // LSB first command bits: 1, 0, 1, 0, 1, 0, 0
        // LSB first address bits: 1, 0, 0, 0, 0
        val pattern = SircEncoder.encode(SonyCodes.POWER, frameCount = 1)

        // Bit 0 of command (LSB) = 1: mark should be 1200
        assertEquals(1200, pattern[2])  // First command bit mark (bit 0 = 1)
        assertEquals(600, pattern[3])   // Space after

        // Bit 1 of command = 0: mark should be 600
        assertEquals(600, pattern[4])   // Second command bit mark (bit 1 = 0)
        assertEquals(600, pattern[5])   // Space after

        // Bit 2 of command = 1: mark should be 1200
        assertEquals(1200, pattern[6])  // Third command bit mark (bit 2 = 1)
    }

    @Test
    fun `frame repetition produces 3x single frame with gaps`() {
        val singlePattern = SircEncoder.encode(SonyCodes.POWER, frameCount = 1)
        val triplePattern = SircEncoder.encode(SonyCodes.POWER, frameCount = 3)

        // Triple should be 3x single frame size
        assertEquals(singlePattern.size * 3, triplePattern.size)
    }

    @Test
    fun `carrier frequency is 40kHz`() {
        assertEquals(40_000, SircEncoder.CARRIER_FREQUENCY)
    }

    @Test
    fun `mark values are only 600, 1200, or 2400`() {
        val validMarks = setOf(600, 1200, 2400)
        SonyCodes.ALL_COMMANDS.forEach { command ->
            val pattern = SircEncoder.encode(command, frameCount = 1)
            // Check mark values (even indices)
            for (i in pattern.indices step 2) {
                assertTrue(
                    "Mark at index $i for ${command.name} should be 600, 1200, or 2400, was ${pattern[i]}",
                    pattern[i] in validMarks
                )
            }
        }
    }

    @Test
    fun `space values are always 600 in single frame`() {
        val pattern = SircEncoder.encode(SonyCodes.POWER, frameCount = 1)
        // Check space values (odd indices)
        for (i in 1 until pattern.size step 2) {
            assertEquals(
                "Space at index $i should be 600",
                600, pattern[i]
            )
        }
    }
}
