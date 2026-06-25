package com.example.lyraapp.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.lyraapp.data.remote.AuthTokenHolder
import com.example.lyraapp.data.remote.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionStore @Inject constructor(
    @ApplicationContext context: Context,
    private val authTokenHolder: AuthTokenHolder,
) {
    private val dataStore: DataStore<Preferences> = context.authDataStore

    val currentUser: Flow<UserProfile?> = dataStore.data.map { preferences ->
        val firstName = preferences[USER_FIRST_NAME_KEY] ?: return@map null
        val lastName = preferences[USER_LAST_NAME_KEY].orEmpty()
        val phone = preferences[USER_PHONE_KEY].orEmpty()
        val id = preferences[USER_ID_KEY].orEmpty()
        val profileCompleted = preferences[USER_PROFILE_COMPLETED_KEY]?.toBooleanStrictOrNull() ?: false
        UserProfile(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phone,
            profileCompleted = profileCompleted,
        )
    }

    suspend fun hydrateTokenFromStorage() {
        val preferences = dataStore.data.first()
        authTokenHolder.accessToken = preferences[ACCESS_TOKEN_KEY]
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        user: UserDto?,
    ) {
        authTokenHolder.accessToken = accessToken
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            user?.let { dto ->
                preferences[USER_ID_KEY] = dto.id
                preferences[USER_PHONE_KEY] = dto.phone
                preferences[USER_FIRST_NAME_KEY] = dto.firstName.orEmpty()
                preferences[USER_LAST_NAME_KEY] = dto.lastName.orEmpty()
                preferences[USER_PROFILE_COMPLETED_KEY] = dto.profileCompleted.toString()
            }
        }
    }

    suspend fun saveUser(user: UserDto) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_PHONE_KEY] = user.phone
            preferences[USER_FIRST_NAME_KEY] = user.firstName.orEmpty()
            preferences[USER_LAST_NAME_KEY] = user.lastName.orEmpty()
            preferences[USER_PROFILE_COMPLETED_KEY] = user.profileCompleted.toString()
        }
    }

    suspend fun clearSession() {
        authTokenHolder.accessToken = null
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_PHONE_KEY)
            preferences.remove(USER_FIRST_NAME_KEY)
            preferences.remove(USER_LAST_NAME_KEY)
            preferences.remove(USER_PROFILE_COMPLETED_KEY)
            preferences.remove(CURRENT_USER_PHONE_KEY)
            preferences.remove(REGISTERED_USERS_KEY)
        }
    }

    suspend fun getRefreshToken(): String? = dataStore.data.first()[REFRESH_TOKEN_KEY]

    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
        val USER_LAST_NAME_KEY = stringPreferencesKey("user_last_name")
        val USER_PROFILE_COMPLETED_KEY = stringPreferencesKey("user_profile_completed")
    }
}

internal fun UserDto.toProfile(): UserProfile = UserProfile(
    id = id,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    phoneNumber = phone,
    profileCompleted = profileCompleted,
)
