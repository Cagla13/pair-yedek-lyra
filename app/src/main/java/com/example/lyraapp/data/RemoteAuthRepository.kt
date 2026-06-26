package com.example.lyraapp.data

import com.example.lyraapp.data.auth.AuthSessionStore
import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.data.remote.PhoneNumberFormatter
import com.example.lyraapp.data.remote.dto.OtpRequestBody
import com.example.lyraapp.data.remote.dto.OtpVerifyBody
import com.example.lyraapp.data.remote.dto.RefreshTokenBody
import com.example.lyraapp.data.remote.dto.UpdateProfileBody
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAuthRepository @Inject constructor(
    private val api: LyraApiService,
    private val authSessionStore: AuthSessionStore,
) : AuthRepository {

    override val currentUser: Flow<com.example.lyraapp.data.auth.UserProfile?> =
        authSessionStore.currentUser

    override suspend fun hydrateSession(): Result<Boolean> = runCatching {
        authSessionStore.hydrateTokenFromStorage()
        val hasToken = !authSessionStore.getAccessToken().isNullOrBlank() ||
            !authSessionStore.getRefreshToken().isNullOrBlank()
        if (!hasToken) {
            return@runCatching false
        }
        fetchCurrentUser().getOrThrow()
        true
    }.recoverCatching {
        authSessionStore.clearSession()
        false
    }

    override suspend fun fetchCurrentUser(): Result<Unit> = try {
        val response = api.getCurrentUser()
        authSessionStore.saveUser(response.data)
        Result.success(Unit)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    override suspend fun logout(): Result<Unit> = try {
        val refreshToken = authSessionStore.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            runCatching { api.logout(RefreshTokenBody(refreshToken)) }
        }
        authSessionStore.clearSession()
        Result.success(Unit)
    } catch (exception: Exception) {
        authSessionStore.clearSession()
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    override suspend fun requestOtp(phoneNumber: String): Result<OtpRequestResult> = try {
        val apiPhone = PhoneNumberFormatter.toApiPhone(phoneNumber)
        require(apiPhone.filter { it.isDigit() }.length >= 10) {
            "Geçerli bir telefon numarası girin."
        }
        val response = api.requestOtp(OtpRequestBody(phone = apiPhone))
        Result.success(
            OtpRequestResult(
                apiPhone = apiPhone,
                firstTime = response.data.firstTime,
            ),
        )
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    override suspend fun verifyOtp(phoneNumber: String, code: String): Result<OtpVerifyResult> = try {
        val apiPhone = PhoneNumberFormatter.toApiPhone(phoneNumber)
        val response = api.verifyOtp(
            OtpVerifyBody(
                phone = apiPhone,
                code = code.trim(),
            ),
        )
        val session = response.data
        authSessionStore.saveSession(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            user = session.user,
        )
        val user = session.user
        Result.success(
            OtpVerifyResult(
                firstTime = session.firstTime || user?.profileCompleted == false,
                profileCompleted = user?.profileCompleted == true,
            ),
        )
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    override suspend fun completeProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
    ): Result<Unit> = try {
        val response = api.updateProfile(
            UpdateProfileBody(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                birthDate = birthDate.trim(),
            ),
        )
        authSessionStore.saveUser(response.data)
        Result.success(Unit)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }
}
