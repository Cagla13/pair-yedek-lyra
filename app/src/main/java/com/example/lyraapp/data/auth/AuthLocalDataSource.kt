package com.example.lyraapp.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthLocalDataSource @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.authDataStore

    val currentUser: Flow<UserProfile?> = dataStore.data.map { preferences ->
        val currentPhone = preferences[CURRENT_USER_PHONE_KEY] ?: return@map null
        parseUsers(preferences[REGISTERED_USERS_KEY].orEmpty())
            .find { it.normalizedPhone == currentPhone }
            ?.toProfile()
    }

    suspend fun registerUser(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit> {
        val normalizedPhone = normalizePhoneNumber(phoneNumber)
        if (normalizedPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçerli bir telefon numarası girin."))
        }

        return try {
            dataStore.edit { preferences ->
                val users = parseUsers(preferences[REGISTERED_USERS_KEY].orEmpty()).toMutableList()
                if (users.any { it.normalizedPhone == normalizedPhone }) {
                    throw DuplicatePhoneException()
                }

                users += StoredUser(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phoneNumber = phoneNumber.trim(),
                    normalizedPhone = normalizedPhone,
                    password = password,
                )
                preferences[REGISTERED_USERS_KEY] = encodeUsers(users)
                preferences[CURRENT_USER_PHONE_KEY] = normalizedPhone
            }
            Result.success(Unit)
        } catch (_: DuplicatePhoneException) {
            Result.failure(IllegalArgumentException("Bu telefon numarası zaten kayıtlı."))
        }
    }

    suspend fun loginUser(phoneNumber: String, password: String): Result<Unit> {
        val normalizedPhone = normalizePhoneNumber(phoneNumber)
        if (normalizedPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçerli bir telefon numarası girin."))
        }

        val preferences = dataStore.data.first()
        val users = parseUsers(preferences[REGISTERED_USERS_KEY].orEmpty())
        val matchedUser = users.find { it.normalizedPhone == normalizedPhone }

        return when {
            matchedUser == null -> Result.failure(
                IllegalArgumentException("Bu telefon numarasıyla kayıtlı hesap bulunamadı."),
            )
            matchedUser.password != password -> Result.failure(
                IllegalArgumentException("Telefon numarası veya şifre hatalı."),
            )
            else -> {
                dataStore.edit { prefs ->
                    prefs[CURRENT_USER_PHONE_KEY] = normalizedPhone
                }
                Result.success(Unit)
            }
        }
    }

    private fun parseUsers(json: String): List<StoredUser> {
        if (json.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        StoredUser(
                            firstName = item.getString("firstName"),
                            lastName = item.getString("lastName"),
                            phoneNumber = item.getString("phoneNumber"),
                            normalizedPhone = item.getString("normalizedPhone"),
                            password = item.getString("password"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeUsers(users: List<StoredUser>): String {
        val array = JSONArray()
        users.forEach { user ->
            array.put(
                JSONObject()
                    .put("firstName", user.firstName)
                    .put("lastName", user.lastName)
                    .put("phoneNumber", user.phoneNumber)
                    .put("normalizedPhone", user.normalizedPhone)
                    .put("password", user.password),
            )
        }
        return array.toString()
    }

    private class DuplicatePhoneException : Exception()
}
