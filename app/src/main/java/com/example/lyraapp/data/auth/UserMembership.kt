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
    val isActivePremium: Boolean
        get() = status.equals("active", ignoreCase = true)

    val isOneTime: Boolean
        get() = type.equals("one-time", ignoreCase = true)

    fun daysUntilExpiry(): Int? {
        val expiresAtValue = expiresAt ?: return null
        return runCatching {
            val expiryDate = when {
                expiresAtValue.contains("Z") -> {
                    ZonedDateTime.parse(expiresAtValue).toLocalDate()
                }
                expiresAtValue.contains("T") -> {
                    if (expiresAtValue.substringAfter("T").contains("+") ||
                        expiresAtValue.substringAfter("T").contains("-")
                    ) {
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
            if (days > 0) days else 0
        }.getOrNull()
    }

    fun shouldShowExpiryPrompt(): Boolean {
        if (!isActivePremium || !isOneTime) return false
        val days = daysUntilExpiry() ?: return false
        return days in 0..3
    }
}

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
