package com.example.lyraapp.ui.auth.register

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val isLoading: Boolean = false,
    val isRegisterEnabled: Boolean = false,
)

sealed interface RegisterIntent {
    data class FirstNameChanged(val value: String) : RegisterIntent
    data class LastNameChanged(val value: String) : RegisterIntent
    data class BirthDateChanged(val value: String) : RegisterIntent
    data object Submit : RegisterIntent
    data object BackClicked : RegisterIntent
}

sealed interface RegisterEffect {
    data object NavigateToHome : RegisterEffect
    data object NavigateBack : RegisterEffect
    data class ShowError(val message: String) : RegisterEffect
}
