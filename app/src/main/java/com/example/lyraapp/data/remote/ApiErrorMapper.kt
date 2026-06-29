package com.example.lyraapp.data.remote

import com.example.lyraapp.data.remote.dto.ApiErrorEnvelope
import kotlinx.serialization.json.Json
import retrofit2.HttpException

object ApiErrorMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun toMessage(throwable: Throwable): String = when (throwable) {
        is HttpException -> {
            val body = throwable.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                runCatching {
                    json.decodeFromString<ApiErrorEnvelope>(body).error?.message
                }.getOrNull()?.takeIf { it.isNotBlank() }
            } else {
                null
            } ?: when (throwable.code()) {
                401 -> "Doğrulama kodu hatalı."
                400 -> "Geçersiz istek."
                402 -> "Kart reddedildi. Lütfen farklı bir kart deneyin."
                else -> "Sunucu hatası (${throwable.code()})."
            }
        }
        is IllegalArgumentException -> throwable.message ?: "Geçersiz bilgi."
        else -> throwable.message ?: "Beklenmeyen bir hata oluştu."
    }
}
