package com.smartir.remote.ir

/**
 * Sony SIRC protocol variants differ by bit count:
 * - 12-bit: 7 command bits + 5 address bits
 * - 15-bit: 7 command bits + 8 address bits
 * - 20-bit: 7 command bits + 5 address bits + 8 extended bits
 */
enum class SircProtocol(val commandBits: Int, val addressBits: Int, val extendedBits: Int) {
    SIRC_12(7, 5, 0),
    SIRC_15(7, 8, 0),
    SIRC_20(7, 5, 8);

    val totalBits: Int get() = commandBits + addressBits + extendedBits
}

data class SonyIrCommand(
    val name: String,
    val command: Int,
    val address: Int,
    val protocol: SircProtocol,
    val extended: Int = 0,
    val adbKeyCode: String? = null
)

object SonyCodes {
    // Address for standard Sony TV (SIRC 12-bit)
    private const val TV_ADDR_12 = 1       // 5-bit TV address
    private const val TV_ADDR_15 = 0x97    // 8-bit address for smart TV functions (Back, media)

    // --- Power ---
    val POWER = SonyIrCommand("Power", 21, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_POWER")

    // --- Volume ---
    val VOLUME_UP = SonyIrCommand("Vol+", 18, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_VOLUME_UP")
    val VOLUME_DOWN = SonyIrCommand("Vol-", 19, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_VOLUME_DOWN")
    val MUTE = SonyIrCommand("Mute", 20, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_VOLUME_MUTE")

    // --- Number pad ---
    val NUM_0 = SonyIrCommand("0", 0, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_0")
    val NUM_1 = SonyIrCommand("1", 1, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_1")
    val NUM_2 = SonyIrCommand("2", 2, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_2")
    val NUM_3 = SonyIrCommand("3", 3, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_3")
    val NUM_4 = SonyIrCommand("4", 4, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_4")
    val NUM_5 = SonyIrCommand("5", 5, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_5")
    val NUM_6 = SonyIrCommand("6", 6, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_6")
    val NUM_7 = SonyIrCommand("7", 7, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_7")
    val NUM_8 = SonyIrCommand("8", 8, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_8")
    val NUM_9 = SonyIrCommand("9", 9, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_9")

    // --- D-Pad / Navigation (12-bit, same TV address as volume/power) ---
    val DPAD_UP = SonyIrCommand("Up", 116, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_DPAD_UP")
    val DPAD_DOWN = SonyIrCommand("Down", 117, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_DPAD_DOWN")
    val DPAD_LEFT = SonyIrCommand("Left", 52, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_DPAD_LEFT")
    val DPAD_RIGHT = SonyIrCommand("Right", 51, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_DPAD_RIGHT")
    val DPAD_OK = SonyIrCommand("OK", 101, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_DPAD_CENTER")

    // --- Menu / Navigation ---
    val HOME = SonyIrCommand("Home", 96, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_HOME")
    val BACK = SonyIrCommand("Back", 35, TV_ADDR_15, SircProtocol.SIRC_15, adbKeyCode = "KEYCODE_BACK")
    val MENU = SonyIrCommand("Menu", 54, TV_ADDR_15, SircProtocol.SIRC_15)

    // --- Input ---
    val INPUT = SonyIrCommand("Input", 37, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_TV_INPUT")

    // --- Media controls (15-bit, smart TV device 0x97) ---
    val PLAY = SonyIrCommand("Play", 26, TV_ADDR_15, SircProtocol.SIRC_15, adbKeyCode = "KEYCODE_MEDIA_PLAY")
    val PAUSE = SonyIrCommand("Pause", 25, TV_ADDR_15, SircProtocol.SIRC_15, adbKeyCode = "KEYCODE_MEDIA_PAUSE")
    val STOP = SonyIrCommand("Stop", 24, TV_ADDR_15, SircProtocol.SIRC_15, adbKeyCode = "KEYCODE_MEDIA_STOP")
    val REWIND = SonyIrCommand("Rewind", 27, TV_ADDR_15, SircProtocol.SIRC_15, adbKeyCode = "KEYCODE_MEDIA_REWIND")
    val FAST_FORWARD = SonyIrCommand("FFwd", 28, TV_ADDR_15, SircProtocol.SIRC_15, adbKeyCode = "KEYCODE_MEDIA_FAST_FORWARD")

    // --- Display ---
    val DISPLAY = SonyIrCommand("Display", 58, TV_ADDR_12, SircProtocol.SIRC_12, adbKeyCode = "KEYCODE_INFO")

    /** All commands for lookup */
    val ALL_COMMANDS: List<SonyIrCommand> = listOf(
        POWER,
        VOLUME_UP, VOLUME_DOWN, MUTE,
        NUM_0, NUM_1, NUM_2, NUM_3, NUM_4,
        NUM_5, NUM_6, NUM_7, NUM_8, NUM_9,
        DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, DPAD_OK,
        HOME, BACK, MENU, INPUT,
        PLAY, PAUSE, STOP, REWIND, FAST_FORWARD,
        DISPLAY
    )
}
