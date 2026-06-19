package com.example.lyraapp.data.auth

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
) {
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
}

internal data class StoredUser(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val normalizedPhone: String,
    val password: String,
) {
    fun toProfile(): UserProfile = UserProfile(
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
    )
}

internal fun normalizePhoneNumber(phoneNumber: String): String =
    phoneNumber.filter { it.isDigit() }
