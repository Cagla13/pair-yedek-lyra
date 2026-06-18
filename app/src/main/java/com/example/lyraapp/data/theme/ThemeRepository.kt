package com.example.lyraapp.data.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val isDarkTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_THEME_KEY] ?: DEFAULT_DARK_THEME
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_THEME_KEY] = isDark
        }
    }
}
