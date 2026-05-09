package com.smartir.remote.adb

import android.content.Context
import dadb.AdbKeyPair
import java.io.File

/**
 * Manages RSA keypair for ADB authentication.
 * Keys are stored in context.filesDir/.adb/ since ~/.android/ is
 * not writable on Android devices.
 */
object AdbKeyStore {

    private const val KEY_DIR = ".adb"
    private const val PRIVATE_KEY_FILE = "adbkey"
    private const val PUBLIC_KEY_FILE = "adbkey.pub"

    fun getOrCreateKeyPair(context: Context): AdbKeyPair {
        val keyDir = File(context.filesDir, KEY_DIR)
        val privateKeyFile = File(keyDir, PRIVATE_KEY_FILE)
        val publicKeyFile = File(keyDir, PUBLIC_KEY_FILE)

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            return AdbKeyPair.read(privateKeyFile, publicKeyFile)
        }

        keyDir.mkdirs()
        AdbKeyPair.generate(privateKeyFile, publicKeyFile)
        return AdbKeyPair.read(privateKeyFile, publicKeyFile)
    }
}
