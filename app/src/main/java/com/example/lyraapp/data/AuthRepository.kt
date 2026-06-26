package com.example.lyraapp.data

import com.example.lyraapp.data.auth.UserProfile
import kotlinx.coroutines.flow.Flow

data class OtpRequestResult(
    val apiPhone: String,
    val firstTime: Boolean,
)

data class OtpVerifyResult(
    val firstTime: Boolean,
    val profileCompleted: Boolean,
)

interface AuthRepository {

    val currentUser: Flow<UserProfile?>

    suspend fun requestOtp(phoneNumber: String): Result<OtpRequestResult>

    suspend fun verifyOtp(phoneNumber: String, code: String): Result<OtpVerifyResult>

    suspend fun completeProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
    ): Result<Unit>

    suspend fun hydrateSession(): Result<Boolean>

    suspend fun fetchCurrentUser(): Result<Unit>

    suspend fun logout(): Result<Unit>
}
