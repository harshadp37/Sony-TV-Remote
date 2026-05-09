package com.smartir.remote.adb

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Scans the local /24 subnet for devices with ADB port 5555 open.
 */
object SubnetScanner {

    private const val ADB_PORT = 5555
    private const val CONNECT_TIMEOUT_MS = 300

    /**
     * Returns list of IP addresses on the local /24 subnet that have port 5555 open.
     */
    suspend fun scan(context: Context): List<String> = coroutineScope {
        val subnet = getSubnetPrefix(context) ?: return@coroutineScope emptyList()

        (1..254).map { host ->
            async(Dispatchers.IO) {
                val ip = "$subnet.$host"
                if (isPortOpen(ip, ADB_PORT)) ip else null
            }
        }.awaitAll().filterNotNull()
    }

    private fun getSubnetPrefix(context: Context): String? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return null
        @Suppress("DEPRECATION")
        val ip = wifiManager.connectionInfo.ipAddress
        if (ip == 0) return null
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}"
    }

    private fun isPortOpen(host: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
