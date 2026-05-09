package com.smartir.remote.adb

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.adbDataStore: DataStore<Preferences> by preferencesDataStore(name = "adb_prefs")

/**
 * Persists the last connected TV IP address for auto-reconnection.
 */
class AdbPreferences(private val context: Context) {

    companion object {
        private val KEY_LAST_IP = stringPreferencesKey("last_tv_ip")
    }

    suspend fun saveLastIp(ip: String) {
        context.adbDataStore.edit { prefs ->
            prefs[KEY_LAST_IP] = ip
        }
    }

    suspend fun getLastIp(): String? {
        return context.adbDataStore.data
            .map { prefs -> prefs[KEY_LAST_IP] }
            .first()
    }

    suspend fun clearLastIp() {
        context.adbDataStore.edit { prefs ->
            prefs.remove(KEY_LAST_IP)
        }
    }
}
