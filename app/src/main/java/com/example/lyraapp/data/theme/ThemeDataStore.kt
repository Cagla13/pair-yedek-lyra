package com.example.lyraapp.data.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lyra_theme_preferences",
)

internal val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")

/** LyraApp varsayılan teması koyu moddur (bkz. docs/design/00-color-system.md). */
internal const val DEFAULT_DARK_THEME = true
