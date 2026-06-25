package com.example.lyraapp.data.remote

object PhoneNumberFormatter {

    /** Kullanıcı girişini API'nin beklediği E.164 benzeri formata çevirir. */
    fun toApiPhone(rawInput: String): String {
        val digits = rawInput.filter { it.isDigit() }
        return when {
            digits.startsWith("90") && digits.length >= 12 -> "+$digits"
            digits.startsWith("0") && digits.length >= 11 -> "+9${digits.drop(1)}"
            digits.length == 10 -> "+90$digits"
            rawInput.trim().startsWith("+") -> rawInput.trim()
            digits.isNotBlank() -> "+$digits"
            else -> rawInput.trim()
        }
    }

    fun maskForDisplay(apiPhone: String): String {
        val digits = apiPhone.filter { it.isDigit() }
        if (digits.length < 4) return apiPhone
        val lastTwo = digits.takeLast(2)
        return "+${digits.take(2)} ${digits.drop(2).take(3)} *** ** $lastTwo"
    }
}
