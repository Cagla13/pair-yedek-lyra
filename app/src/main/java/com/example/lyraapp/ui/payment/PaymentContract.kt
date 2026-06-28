package com.example.lyraapp.ui.payment

data class PaymentUiState(
    val cardNumber: String = "",
    val cardHolderName: String = "",
    val expiryDate: String = "",
    val cvc: String = "",
    val planName: String = "LyraApp Premium",
    val planDescription: String = "Aylık abonelik",
    val price: String = "₺59,99",
    val isLoading: Boolean = false,
    val isPayEnabled: Boolean = false,
)

sealed interface PaymentIntent {
    data class CardNumberChanged(val value: String) : PaymentIntent
    data class CardHolderNameChanged(val value: String) : PaymentIntent
    data class ExpiryDateChanged(val value: String) : PaymentIntent
    data class CvcChanged(val value: String) : PaymentIntent
    data object PayClicked : PaymentIntent
    data object BackClicked : PaymentIntent
}

sealed interface PaymentEffect {
    data object NavigateBack : PaymentEffect
    data object PaymentSuccess : PaymentEffect
    data class ShowError(val message: String) : PaymentEffect
}
