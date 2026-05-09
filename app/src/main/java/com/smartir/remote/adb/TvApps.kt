package com.smartir.remote.adb

import androidx.compose.ui.graphics.Color

data class TvApp(
    val name: String,
    val packageName: String,
    val color: Color,
    val colorEnd: Color = color
)

object TvApps {
    val YOUTUBE = TvApp(
        name = "YouTube",
        packageName = "com.google.android.youtube.tv",
        color = Color(0xFFFF0000),
        colorEnd = Color(0xFFCC0000)
    )

    val HOTSTAR = TvApp(
        name = "Hotstar",
        packageName = "in.startv.hotstar",
        color = Color(0xFF1F49E0),
        colorEnd = Color(0xFF1A3CB8)
    )

    val KODI = TvApp(
        name = "Kodi",
        packageName = "org.xbmc.kodi",
        color = Color(0xFF2196F3),
        colorEnd = Color(0xFF1976D2)
    )

    val FX_MANAGER = TvApp(
        name = "FX",
        packageName = "nextapp.fx",
        color = Color(0xFFFF9800),
        colorEnd = Color(0xFFE68900)
    )

    val SEND_FILES = TvApp(
        name = "SFTV",
        packageName = "com.yablio.sendfilestotv",
        color = Color(0xFF4CAF50),
        colorEnd = Color(0xFF388E3C)
    )

    val FANCODE = TvApp(
        name = "FanCode",
        packageName = "com.fancode.tv",
        color = Color(0xFF6C3FC5),
        colorEnd = Color(0xFF5630A0)
    )

    val ROW_1 = listOf(HOTSTAR, KODI, YOUTUBE)
    val ROW_2 = listOf(FX_MANAGER, FANCODE, SEND_FILES)
}
