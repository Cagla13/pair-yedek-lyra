package com.example.lyraapp.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "lyra_app_settings")

data class AppSettings(
    val soundQuality: String = "Yüksek",
    val offlineDownloadEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
)

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            soundQuality = preferences[SOUND_QUALITY_KEY] ?: "Yüksek",
            offlineDownloadEnabled = preferences[OFFLINE_DOWNLOAD_KEY] ?: true,
            notificationsEnabled = preferences[NOTIFICATIONS_KEY] ?: true,
        )
    }

    suspend fun cycleSoundQuality() {
        dataStore.edit { preferences ->
            val current = preferences[SOUND_QUALITY_KEY] ?: "Yüksek"
            preferences[SOUND_QUALITY_KEY] = if (current == "Yüksek") "Normal" else "Yüksek"
        }
    }

    suspend fun toggleOfflineDownload() {
        dataStore.edit { preferences ->
            val current = preferences[OFFLINE_DOWNLOAD_KEY] ?: true
            preferences[OFFLINE_DOWNLOAD_KEY] = !current
        }
    }

    suspend fun toggleNotifications() {
        dataStore.edit { preferences ->
            val current = preferences[NOTIFICATIONS_KEY] ?: true
            preferences[NOTIFICATIONS_KEY] = !current
        }
    }

    private companion object {
        val SOUND_QUALITY_KEY = stringPreferencesKey("sound_quality")
        val OFFLINE_DOWNLOAD_KEY = booleanPreferencesKey("offline_download")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
    }
}
