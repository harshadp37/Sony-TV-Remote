package com.smartir.remote.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartir.remote.adb.TvApp
import com.smartir.remote.adb.TvApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.appSettingsStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Serializable
data class CustomApp(
    val id: String,
    val name: String,
    val packageName: String,
    val colorHex: Long
)

fun CustomApp.toTvApp(): TvApp = TvApp(
    id = id,
    name = name,
    packageName = packageName,
    color = Color(colorHex.toInt()),
    colorEnd = Color(colorHex.toInt())
)

class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_SELECTED_IDS = stringPreferencesKey("selected_app_ids")
        private val KEY_CUSTOM_APPS = stringPreferencesKey("custom_apps")
        private val json = Json { ignoreUnknownKeys = true }
    }

    fun selectedAppIdsFlow(): Flow<List<String>> {
        return context.appSettingsStore.data.map { prefs ->
            val raw = prefs[KEY_SELECTED_IDS]
            if (raw == null) {
                TvApps.DEFAULT_SELECTED_IDS
            } else {
                json.decodeFromString<List<String>>(raw)
            }
        }
    }

    suspend fun saveSelectedAppIds(ids: List<String>) {
        context.appSettingsStore.edit { prefs ->
            prefs[KEY_SELECTED_IDS] = json.encodeToString(ids)
        }
    }

    fun customAppsFlow(): Flow<List<CustomApp>> {
        return context.appSettingsStore.data.map { prefs ->
            val raw = prefs[KEY_CUSTOM_APPS]
            if (raw == null) {
                emptyList()
            } else {
                json.decodeFromString<List<CustomApp>>(raw)
            }
        }
    }

    suspend fun saveCustomApps(apps: List<CustomApp>) {
        context.appSettingsStore.edit { prefs ->
            prefs[KEY_CUSTOM_APPS] = json.encodeToString(apps)
        }
    }
}
