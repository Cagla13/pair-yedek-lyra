package com.example.lyraapp.data.membership

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.premiumPromptDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "premium_prompt",
)

@Singleton
class PremiumPromptStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.premiumPromptDataStore

    suspend fun isDismissed(expiresAt: String): Boolean {
        val dismissedFor = dataStore.data.first()[DISMISSED_EXPIRES_AT_KEY]
        return dismissedFor == expiresAt
    }

    suspend fun dismiss(expiresAt: String) {
        dataStore.edit { preferences ->
            preferences[DISMISSED_EXPIRES_AT_KEY] = expiresAt
        }
    }

    private companion object {
        val DISMISSED_EXPIRES_AT_KEY = stringPreferencesKey("dismissed_expires_at")
    }
}
