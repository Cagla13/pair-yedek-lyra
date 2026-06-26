package com.example.lyraapp.data.remote

import com.example.lyraapp.data.auth.AuthSessionStore
import com.example.lyraapp.data.remote.dto.AuthTokensDto
import com.example.lyraapp.data.remote.dto.RefreshTokenBody
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshManager @Inject constructor(
    private val authSessionStore: AuthSessionStore,
    private val authTokenHolder: AuthTokenHolder,
    private val json: Json,
) {
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val lock = Any()

    fun refreshAccessTokenBlocking(): Boolean = synchronized(lock) {
        runBlocking { refreshAccessToken() }
    }

    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = authSessionStore.getRefreshToken() ?: return false
        val body = json.encodeToString(RefreshTokenBody.serializer(), RefreshTokenBody(refreshToken))
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/api/v1/auth/refresh")
            .post(body)
            .build()

        return runCatching {
            refreshClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                val payload = response.body?.string().orEmpty()
                val envelope = json.decodeFromString<com.example.lyraapp.data.remote.dto.ApiEnvelope<com.example.lyraapp.data.remote.dto.AuthTokensDto>>(payload)
                authSessionStore.updateTokens(
                    accessToken = envelope.data.accessToken,
                    refreshToken = envelope.data.refreshToken,
                )
                true
            }
        }.getOrDefault(false)
    }

    private companion object {
        private const val BASE_URL = "https://streaming-api.halitkalayci.com/"
    }
}
