package com.example.lyraapp.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
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
        val birthDate = preferences[USER_BIRTH_DATE_KEY].orEmpty()
        UserProfile(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phone,
            birthDate = birthDate,
            profileCompleted = profileCompleted,
            membership = readMembership(preferences),
        )
    }

    suspend fun hydrateTokenFromStorage() {
        val preferences = dataStore.data.first()
        authTokenHolder.accessToken = preferences[ACCESS_TOKEN_KEY]
    }

    suspend fun updateTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        authTokenHolder.accessToken = accessToken
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
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
            user?.let { dto -> writeUserProfile(preferences, dto) }
                ?: writeMembership(preferences, null)
        }
    }

    suspend fun saveUser(user: UserDto) {
        dataStore.edit { preferences ->
            writeUserProfile(preferences, user)
        }
    }

    private fun writeUserProfile(preferences: androidx.datastore.preferences.core.MutablePreferences, user: UserDto) {
        preferences[USER_ID_KEY] = user.id
        preferences[USER_PHONE_KEY] = user.phone
        preferences[USER_FIRST_NAME_KEY] = user.firstName.orEmpty()
        preferences[USER_LAST_NAME_KEY] = user.lastName.orEmpty()
        preferences[USER_PROFILE_COMPLETED_KEY] = user.profileCompleted.toString()
        preferences[USER_BIRTH_DATE_KEY] = user.birthDate.orEmpty()
        writeMembership(preferences, user.membership?.toUserMembership())
    }

    private fun writeMembership(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        membership: UserMembership?,
    ) {
        if (membership == null) {
            preferences.remove(MEMBERSHIP_PLAN_ID_KEY)
            preferences.remove(MEMBERSHIP_TYPE_KEY)
            preferences.remove(MEMBERSHIP_STATUS_KEY)
            preferences.remove(MEMBERSHIP_AUTO_RENEW_KEY)
            preferences.remove(MEMBERSHIP_EXPIRES_AT_KEY)
            return
        }
        preferences[MEMBERSHIP_PLAN_ID_KEY] = membership.planId
        preferences[MEMBERSHIP_TYPE_KEY] = membership.type
        preferences[MEMBERSHIP_STATUS_KEY] = membership.status
        preferences[MEMBERSHIP_AUTO_RENEW_KEY] = membership.autoRenew
        membership.expiresAt?.let { preferences[MEMBERSHIP_EXPIRES_AT_KEY] = it }
            ?: preferences.remove(MEMBERSHIP_EXPIRES_AT_KEY)
    }

    private fun readMembership(preferences: Preferences): UserMembership? {
        val type = preferences[MEMBERSHIP_TYPE_KEY] ?: return null
        val status = preferences[MEMBERSHIP_STATUS_KEY] ?: return null
        return UserMembership(
            planId = preferences[MEMBERSHIP_PLAN_ID_KEY].orEmpty(),
            type = type,
            status = status,
            autoRenew = preferences[MEMBERSHIP_AUTO_RENEW_KEY] ?: false,
            expiresAt = preferences[MEMBERSHIP_EXPIRES_AT_KEY],
        )
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
            preferences.remove(USER_BIRTH_DATE_KEY)
            preferences.remove(MEMBERSHIP_PLAN_ID_KEY)
            preferences.remove(MEMBERSHIP_TYPE_KEY)
            preferences.remove(MEMBERSHIP_STATUS_KEY)
            preferences.remove(MEMBERSHIP_AUTO_RENEW_KEY)
            preferences.remove(MEMBERSHIP_EXPIRES_AT_KEY)
            preferences.remove(CURRENT_USER_PHONE_KEY)
            preferences.remove(REGISTERED_USERS_KEY)
        }
    }

    suspend fun getRefreshToken(): String? = dataStore.data.first()[REFRESH_TOKEN_KEY]

    suspend fun getAccessToken(): String? = dataStore.data.first()[ACCESS_TOKEN_KEY]

    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
        val USER_LAST_NAME_KEY = stringPreferencesKey("user_last_name")
        val USER_PROFILE_COMPLETED_KEY = stringPreferencesKey("user_profile_completed")
        val USER_BIRTH_DATE_KEY = stringPreferencesKey("user_birth_date")
        val MEMBERSHIP_PLAN_ID_KEY = stringPreferencesKey("membership_plan_id")
        val MEMBERSHIP_TYPE_KEY = stringPreferencesKey("membership_type")
        val MEMBERSHIP_STATUS_KEY = stringPreferencesKey("membership_status")
        val MEMBERSHIP_AUTO_RENEW_KEY = booleanPreferencesKey("membership_auto_renew")
        val MEMBERSHIP_EXPIRES_AT_KEY = stringPreferencesKey("membership_expires_at")
    }
}

internal fun UserDto.toProfile(): UserProfile = UserProfile(
    id = id,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    phoneNumber = phone,
    birthDate = birthDate.orEmpty(),
    profileCompleted = profileCompleted,
    membership = membership?.toUserMembership(),
)
