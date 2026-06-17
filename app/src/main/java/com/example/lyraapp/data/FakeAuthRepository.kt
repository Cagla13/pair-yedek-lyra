package com.example.lyraapp.data

import kotlinx.coroutines.delay
import javax.inject.Inject


class FakeAuthRepository @Inject constructor() : AuthRepository {

    override suspend fun login(phoneNumber: String, password: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return if (password.isNotBlank()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Şifre boş olamaz."))
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return if (firstName.isNotBlank() && lastName.isNotBlank() && password.isNotBlank()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Hesap bilgileri eksik."))
        }
    }

    private companion object {
        const val NETWORK_DELAY_MS = 1_000L
    }
}