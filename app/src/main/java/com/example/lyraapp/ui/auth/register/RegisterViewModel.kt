package com.example.lyraapp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RegisterEffect>(Channel.BUFFERED)
    val effect: Flow<RegisterEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is RegisterIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is RegisterIntent.BirthDateChanged -> updateForm { it.copy(birthDate = intent.value) }
            RegisterIntent.Submit -> submit()
            RegisterIntent.BackClicked -> sendEffect(RegisterEffect.NavigateBack)
        }
    }

    private fun updateForm(transform: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isRegisterEnabled = updated.isFormValid())
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isRegisterEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.completeProfile(
                firstName = state.firstName,
                lastName = state.lastName,
                birthDate = state.birthDate,
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { _effect.send(RegisterEffect.NavigateToHome) }
                .onFailure { error ->
                    _effect.send(RegisterEffect.ShowError(error.message ?: "Profil kaydedilemedi."))
                }
        }
    }

    private fun sendEffect(effect: RegisterEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

private fun RegisterUiState.isFormValid(): Boolean =
    firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        birthDate.matches(BIRTH_DATE_REGEX)

private val BIRTH_DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
