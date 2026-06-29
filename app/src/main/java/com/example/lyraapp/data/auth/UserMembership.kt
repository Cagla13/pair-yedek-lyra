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
    val startedAt: String? = null,
    val expiresAt: String? = null,
) {
    val isActivePremium: Boolean
        get() = status.equals("active", ignoreCase = true)

    val isOneTime: Boolean
        get() = type.equals("one-time", ignoreCase = true)

    /** Gerçek premium bitişi (API expiresAt) — oynatma / profil kalan gün. */
    fun daysUntilExpiry(): Int? {
        val expiryDate = parseToLocalDate(expiresAt) ?: return null
        return daysBetweenTodayAnd(expiryDate)
    }

    /**
     * One-time plan pop-up penceresi: API 30 gün verse bile uyarı [startedAt + 3 gün] üzerinden.
     * Recurring planda kullanılmaz.
     */
    fun daysUntilExpiryPrompt(): Int? {
        if (!isOneTime) return daysUntilExpiry()
        val promptExpiry = oneTimePromptExpiryDate() ?: return null
        return daysBetweenTodayAnd(promptExpiry)
    }

    fun shouldShowExpiryPrompt(): Boolean {
        if (!isActivePremium || !isOneTime) return false
        val promptExpiry = oneTimePromptExpiryDate() ?: return false
        val today = LocalDate.now()
        if (today.isAfter(promptExpiry)) return false
        val days = ChronoUnit.DAYS.between(today, promptExpiry).toInt()
        return days in 0..3
    }

    /** Pop-up dismiss anahtarı — one-time için hesaplanan 3 günlük pencere. */
    fun expiryPromptDismissKey(): String? {
        if (!isOneTime) return expiresAt
        return oneTimePromptExpiryDate()?.toString() ?: expiresAt
    }

    private fun oneTimePromptExpiryDate(): LocalDate? {
        parseToLocalDate(startedAt)?.let { start ->
            return start.plusDays(ONE_TIME_PROMPT_WINDOW_DAYS)
        }
        // startedAt yoksa: API'nin verdiği 30 günlük sürenin son 3 günü yerine
        // bitişten 27 gün öncesini başlangıç kabul et (30 → 3 gün penceresi).
        parseToLocalDate(expiresAt)?.let { apiExpiry ->
            return apiExpiry.minusDays(API_ONE_TIME_DURATION_DAYS - ONE_TIME_PROMPT_WINDOW_DAYS)
        }
        return LocalDate.now().plusDays(ONE_TIME_PROMPT_WINDOW_DAYS)
    }

    private fun daysBetweenTodayAnd(expiryDate: LocalDate): Int {
        val days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate).toInt()
        return if (days > 0) days else 0
    }

    private fun parseToLocalDate(value: String?): LocalDate? {
        val raw = value?.takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            when {
                raw.contains("Z") -> ZonedDateTime.parse(raw).toLocalDate()
                raw.contains("T") -> {
                    if (raw.substringAfter("T").contains("+") ||
                        raw.substringAfter("T").contains("-")
                    ) {
                        OffsetDateTime.parse(raw).toLocalDate()
                    } else {
                        LocalDateTime.parse(raw).toLocalDate()
                    }
                }
                else -> LocalDate.parse(raw)
            }
        }.getOrNull()
    }

    private companion object {
        /** Pop-up'ta gösterilen one-time uyarı penceresi (gün). */
        const val ONE_TIME_PROMPT_WINDOW_DAYS = 3L
        /** API one-time plan varsayılan süresi (checkout durationDays). */
        const val API_ONE_TIME_DURATION_DAYS = 30L
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
        startedAt = startedAt,
        expiresAt = expiresAt,
    )
}
