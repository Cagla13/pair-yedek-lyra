package com.example.lyraapp.data.auth

import com.example.lyraapp.data.remote.dto.MembershipDto
import java.time.Instant
import java.time.temporal.ChronoUnit

data class UserMembership(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val expiresAt: String?,
) {
    val isActivePremium: Boolean
        get() = status == "active"

    val isOneTime: Boolean
        get() = type == "one-time"

    fun daysUntilExpiry(): Int? {
        val expiresAtValue = expiresAt ?: return null
        return runCatching {
            val expiry = Instant.parse(expiresAtValue)
            ChronoUnit.DAYS.between(Instant.now(), expiry).toInt()
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
