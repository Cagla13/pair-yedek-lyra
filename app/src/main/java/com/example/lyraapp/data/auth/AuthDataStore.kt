package com.example.lyraapp.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lyra_auth_preferences",
)

internal val REGISTERED_USERS_KEY = stringPreferencesKey("registered_users")
internal val CURRENT_USER_PHONE_KEY = stringPreferencesKey("current_user_phone")
