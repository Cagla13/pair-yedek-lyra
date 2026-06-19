package com.example.lyraapp.data

import com.example.lyraapp.data.auth.AuthLocalDataSource
import com.example.lyraapp.data.auth.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FakeAuthRepository @Inject constructor(
    private val authLocalDataSource: AuthLocalDataSource,
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = authLocalDataSource.currentUser

    override suspend fun login(phoneNumber: String, password: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return authLocalDataSource.loginUser(phoneNumber, password)
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return authLocalDataSource.registerUser(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            password = password,
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L
    }
}
