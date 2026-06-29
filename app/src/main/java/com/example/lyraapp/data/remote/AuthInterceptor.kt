package com.example.lyraapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authTokenHolder: AuthTokenHolder,
    private val tokenRefreshManager: TokenRefreshManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val authenticatedRequest = original.withBearerToken()
        var response = chain.proceed(authenticatedRequest)

       if (response.code == 401 && !original.url.encodedPath.contains("/auth/")) {

            val isRefreshSuccessful = tokenRefreshManager.refreshAccessTokenBlocking()

            if (isRefreshSuccessful) {
                 response.close()
                response = chain.proceed(original.withBearerToken())
            }
                }

        return response
    }

    private fun okhttp3.Request.withBearerToken(): okhttp3.Request {
        val token = authTokenHolder.accessToken
        return if (!token.isNullOrBlank()) {
            newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            this
        }
    }
}