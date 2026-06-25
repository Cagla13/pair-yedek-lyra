package com.example.lyraapp.ui.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.data.remote.PhoneNumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val phoneNumber: String = savedStateHandle.get<String>(PHONE_ARG).orEmpty()

    private val _uiState = MutableStateFlow(
        OtpUiState(
            phoneNumber = phoneNumber,
            maskedPhone = PhoneNumberFormatter.maskForDisplay(phoneNumber),
            resendSecondsRemaining = RESEND_COOLDOWN_SECONDS,
        ),
    )
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(Channel.BUFFERED)
    val effect: Flow<OtpEffect> = _effect.receiveAsFlow()

    private var resendJob: Job? = null

    init {
        startResendCountdown()
    }

    fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.OtpCodeChanged -> {
                val digits = intent.value.filter { it.isDigit() }.take(OTP_LENGTH)
                _uiState.update {
                    it.copy(
                        otpCode = digits,
                        isVerifyEnabled = digits.length == OTP_LENGTH,
                    )
                }
            }
            OtpIntent.Verify -> verify()
            OtpIntent.Resend -> resend()
            OtpIntent.Back -> viewModelScope.launch { _effect.send(OtpEffect.NavigateBack) }
        }
    }

    private fun verify() {
        val state = _uiState.value
        if (!state.isVerifyEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.verifyOtp(state.phoneNumber, state.otpCode)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { verifyResult ->
                    if (verifyResult.firstTime || !verifyResult.profileCompleted) {
                        _effect.send(OtpEffect.NavigateToCompleteProfile)
                    } else {
                        _effect.send(OtpEffect.NavigateToHome)
                    }
                }
                .onFailure { error ->
                    _effect.send(OtpEffect.ShowError(error.message ?: "Doğrulama başarısız."))
                }
        }
    }

    private fun resend() {
        val state = _uiState.value
        if (state.resendSecondsRemaining > 0 || state.isResendLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResendLoading = true) }
            val result = authRepository.requestOtp(state.phoneNumber)
            _uiState.update { it.copy(isResendLoading = false) }

            result
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            otpCode = "",
                            isVerifyEnabled = false,
                            resendSecondsRemaining = RESEND_COOLDOWN_SECONDS,
                        )
                    }
                    startResendCountdown()
                    _effect.send(OtpEffect.ShowMessage("Doğrulama kodu tekrar gönderildi."))
                }
                .onFailure { error ->
                    _effect.send(OtpEffect.ShowError(error.message ?: "Kod tekrar gönderilemedi."))
                }
        }
    }

    private fun startResendCountdown() {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            while (_uiState.value.resendSecondsRemaining > 0) {
                delay(1_000)
                _uiState.update { it.copy(resendSecondsRemaining = it.resendSecondsRemaining - 1) }
            }
        }
    }

    companion object {
        const val PHONE_ARG = "phone"
        private const val OTP_LENGTH = 6
        private const val RESEND_COOLDOWN_SECONDS = 60
    }
}
