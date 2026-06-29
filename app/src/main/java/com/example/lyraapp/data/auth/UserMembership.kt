package com.example.lyraapp.data.auth

import com.example.lyraapp.data.remote.dto.MembershipDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class UserMembership(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val expiresAt: String?,
) {
    // API'den "active" veya "ACTIVE" gelse bile sorunsuz çalışması için güvenli hale getirildi
    val isActivePremium: Boolean
        get() = status.equals("active", ignoreCase = true)

    val isOneTime: Boolean
        get() = type.equals("one-time", ignoreCase = true)

    /**
     * API'den gelen ISO-8601 tarih formatı ne olursa olsun (Z içeren/içermeyen)
     * güvenli bir şekilde gün farkını hesaplayan fonksiyon.
     */
    fun daysUntilExpiry(): Int? {
        val expiresAtValue = expiresAt ?: return null
        return runCatching {
            // Tarih formatındaki pürüzleri gidermek için esnek çözümleme mantığı
            val expiryDate = when {
                expiresAtValue.contains("Z") -> {
                    ZonedDateTime.parse(expiresAtValue).toLocalDate()
                }
                expiresAtValue.contains("T") -> {
                    // Eğer '+' veya '-' gibi zaman dilimi (offset) bilgisi varsa
                    if (expiresAtValue.substringAfter("T").contains("+") ||
                        expiresAtValue.substringAfter("T").contains("-")) {
                        OffsetDateTime.parse(expiresAtValue).toLocalDate()
                    } else {
                        LocalDateTime.parse(expiresAtValue).toLocalDate()
                    }
                }
                else -> {
                    LocalDate.parse(expiresAtValue)
                }
            }

            val today = LocalDate.now()
            val days = ChronoUnit.DAYS.between(today, expiryDate).toInt()

            // Eğer bitiş tarihi geçmişse veya bugünse 0, gelecekse gün sayısını döner
            if (days > 0) days else 0
        }.getOrNull()
    }

    fun shouldShowExpiryPrompt(): Boolean {
        if (!isActivePremium || !isOneTime) return false
        val days = daysUntilExpiry() ?: return false
        return days in 0..3
    }
}

/**
 * MembershipDto'yu domain modeline güvenli bir şekilde dönüştüren uzantı fonksiyonu.
 */
internal fun MembershipDto.toUserMembership(): UserMembership? {
    val membershipType = type ?: return null
    val membershipStatus = status ?: return null
    return UserMembership(
        planId = planId.orEmpty(),
        type = membershipType,
        status = membershipStatus,
        autoRenew = autoRenew,
        expiresAt = expiresAt,
    )
}