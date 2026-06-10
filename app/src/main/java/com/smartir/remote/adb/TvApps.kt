package com.smartir.remote.adb

import androidx.compose.ui.graphics.Color

data class TvApp(
    val id: String,
    val name: String,
    val packageName: String,
    val color: Color,
    val colorEnd: Color = color
)

object TvApps {
    val HOTSTAR = TvApp(
        id = "hotstar",
        name = "Hotstar",
        packageName = "in.startv.hotstar",
        color = Color(0xFF1F49E0),
        colorEnd = Color(0xFF1A3CB8)
    )

    val KODI = TvApp(
        id = "kodi",
        name = "Kodi",
        packageName = "org.xbmc.kodi",
        color = Color(0xFF2196F3),
        colorEnd = Color(0xFF1976D2)
    )

    val YOUTUBE = TvApp(
        id = "youtube",
        name = "YouTube",
        packageName = "com.google.android.youtube.tv",
        color = Color(0xFFFF0000),
        colorEnd = Color(0xFFCC0000)
    )

    val FX_MANAGER = TvApp(
        id = "fx",
        name = "FX",
        packageName = "nextapp.fx",
        color = Color(0xFFFF9800),
        colorEnd = Color(0xFFE68900)
    )

    val FANCODE = TvApp(
        id = "fancode",
        name = "FanCode",
        packageName = "com.fancode.tv",
        color = Color(0xFF6C3FC5),
        colorEnd = Color(0xFF5630A0)
    )

    val SEND_FILES = TvApp(
        id = "sftv",
        name = "SFTV",
        packageName = "com.yablio.sendfilestotv",
        color = Color(0xFF4CAF50),
        colorEnd = Color(0xFF388E3C)
    )

    val ZEE5 = TvApp(
        id = "zee5",
        name = "Zee5",
        packageName = "com.graymatrix.did",
        color = Color(0xFF8B24AA),
        colorEnd = Color(0xFF6A1B8A)
    )

    val NETFLIX = TvApp(
        id = "netflix",
        name = "Netflix",
        packageName = "com.netflix.ninja",
        color = Color(0xFFE50914),
        colorEnd = Color(0xFFB20710)
    )

    val PRIME_VIDEO = TvApp(
        id = "prime",
        name = "Prime",
        packageName = "com.amazon.amazonvideo.livingroom",
        color = Color(0xFF00A8E1),
        colorEnd = Color(0xFF1A73B5)
    )

    val SONYLIV = TvApp(
        id = "sonyliv",
        name = "SonyLIV",
        packageName = "com.sonyliv",
        color = Color(0xFF1A237E),
        colorEnd = Color(0xFF0D1542)
    )

    val MX_PLAYER = TvApp(
        id = "mx",
        name = "MX Player",
        packageName = "com.mxtech.videoplayer.ad",
        color = Color(0xFF0D47A1),
        colorEnd = Color(0xFF08306B)
    )

    val APPLE_TV = TvApp(
        id = "appletv",
        name = "Apple TV",
        packageName = "com.apple.atve.androidtv.appletv",
        color = Color(0xFF424242),
        colorEnd = Color(0xFF212121)
    )

    val CATALOG: List<TvApp> = listOf(
        HOTSTAR, KODI, YOUTUBE, FX_MANAGER, FANCODE, SEND_FILES,
        ZEE5, NETFLIX, PRIME_VIDEO, SONYLIV, MX_PLAYER, APPLE_TV
    )

    val DEFAULT_SELECTED_IDS = listOf("hotstar", "kodi", "youtube", "fx", "fancode", "sftv")

    fun findById(id: String): TvApp? = CATALOG.find { it.id == id }
}
