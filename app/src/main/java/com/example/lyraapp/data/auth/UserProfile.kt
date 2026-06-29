package com.example.lyraapp.data.auth

data class UserProfile(
    val id: String = "",
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val birthDate: String = "",
    val profileCompleted: Boolean = true,
    val membership: UserMembership? = null,
) {
    val isPremium: Boolean
        get() = membership?.isActivePremium == true

    val fullName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")

    val initials: String
        get() {
            val first = firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            val last = lastName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            return "$first$last"
        }

    val handle: String
        get() {
            val userPart = firstName.trim().lowercase().replace(" ", "")
            val lastInitial = lastName.trim().firstOrNull()?.lowercaseChar()?.toString().orEmpty()
            return "@$userPart$lastInitial"
        }

    // Kendi yazdığın gün hesaplama fonksiyonunu doğrudan buraya bağladık!
    val premiumRemainingDays: Int?
        get() = membership?.daysUntilExpiry()
}