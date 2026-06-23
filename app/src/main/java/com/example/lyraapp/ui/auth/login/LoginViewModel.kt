package com.example.lyraapp.ui.auth.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged -> updateForm { it.copy(phoneNumber = intent.value) }
            LoginIntent.Submit -> submit()
        }
    }

    private fun updateForm(transform: (LoginUiState) -> LoginUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isContinueEnabled = updated.phoneNumber.filter { it.isDigit() }.length >= 10)
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isContinueEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.requestOtp(state.phoneNumber)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { otpResult ->
                    _effect.send(LoginEffect.NavigateToOtp(otpResult.apiPhone))
                }
                .onFailure { error ->
                    _effect.send(LoginEffect.ShowError(error.message ?: "Kod gönderilemedi."))
                }
        }
    }
}
