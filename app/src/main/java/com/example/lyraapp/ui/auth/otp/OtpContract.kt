package com.example.lyraapp.ui.auth.otp

data class OtpUiState(
    val phoneNumber: String = "",
    val maskedPhone: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val isResendLoading: Boolean = false,
    val isVerifyEnabled: Boolean = false,
    val resendSecondsRemaining: Int = 0,
)

sealed interface OtpIntent {
    data class OtpCodeChanged(val value: String) : OtpIntent
    data object Verify : OtpIntent
    data object Resend : OtpIntent
    data object Back : OtpIntent
}

sealed interface OtpEffect {
    data object NavigateToHome : OtpEffect
    data object NavigateToCompleteProfile : OtpEffect
    data object NavigateBack : OtpEffect
    data class ShowError(val message: String) : OtpEffect
    data class ShowMessage(val message: String) : OtpEffect
}
