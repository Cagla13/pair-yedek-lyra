package com.example.lyraapp.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.data.membership.CheckoutCardDetails
import com.example.lyraapp.data.membership.MembershipRepository
import com.example.lyraapp.ui.navigation.LyraDestination
import com.example.lyraapp.ui.premium.PremiumPlanType
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
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val membershipRepository: MembershipRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val planType = savedStateHandle.get<String>(LyraDestination.Payment.PLAN_ARG)
        ?: PremiumPlanType.RECURRING.apiValue
    private val price = savedStateHandle.get<String>(LyraDestination.Payment.PRICE_ARG) ?: "₺59,99"
    private val title = savedStateHandle.get<String>(LyraDestination.Payment.TITLE_ARG) ?: "LyraApp Premium"
    private val desc = savedStateHandle.get<String>(LyraDestination.Payment.DESC_ARG) ?: "Aylık abonelik"

    private val _uiState = MutableStateFlow(
        PaymentUiState(
            price = price,
            planName = title,
            planDescription = desc,
        ),
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentEffect>(Channel.BUFFERED)
    val effect: Flow<PaymentEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.CardNumberChanged -> updateForm { it.copy(cardNumber = formatCardNumber(intent.value)) }
            is PaymentIntent.CardHolderNameChanged -> updateForm { it.copy(cardHolderName = intent.value) }
            is PaymentIntent.ExpiryDateChanged -> updateForm { it.copy(expiryDate = formatExpiryDate(intent.value)) }
            is PaymentIntent.CvcChanged -> updateForm { it.copy(cvc = intent.value.take(3).filter { it.isDigit() }) }
            PaymentIntent.PayClicked -> processPayment()
            PaymentIntent.BackClicked -> viewModelScope.launch { _effect.send(PaymentEffect.NavigateBack) }
        }
    }

    private fun updateForm(transform: (PaymentUiState) -> PaymentUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isPayEnabled = validate(updated))
        }
    }

    private fun validate(state: PaymentUiState): Boolean {
        return state.cardNumber.replace(" ", "").length == 16 &&
            state.cardHolderName.isNotBlank() &&
            state.expiryDate.length == 5 &&
            state.cvc.length == 3
    }

    private fun formatCardNumber(value: String): String {
        val digits = value.filter { it.isDigit() }.take(16)
        return digits.chunked(4).joinToString(" ")
    }

    private fun formatExpiryDate(value: String): String {
        val digits = value.filter { it.isDigit() }.take(4)
        return if (digits.length >= 3) {
            "${digits.substring(0, 2)}/${digits.substring(2)}"
        } else {
            digits
        }
    }

    private fun processPayment() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value
        val expiry = parseExpiry(state.expiryDate)
        if (expiry == null) {
            viewModelScope.launch {
                _effect.send(PaymentEffect.ShowError("Geçersiz son kullanma tarihi."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            membershipRepository.checkout(
                planType = planType,
                card = CheckoutCardDetails(
                    number = state.cardNumber.replace(" ", ""),
                    expMonth = expiry.first,
                    expYear = expiry.second,
                    cvc = state.cvc,
                    holderName = state.cardHolderName.trim(),
                ),
            ).onSuccess {
                authRepository.fetchCurrentUser()
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(PaymentEffect.PaymentSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(PaymentEffect.ShowError(error.message ?: "Ödeme başarısız."))
            }
        }
    }

    private fun parseExpiry(expiryDate: String): Pair<Int, Int>? {
        val parts = expiryDate.split("/")
        if (parts.size != 2) return null
        val month = parts[0].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
        val yearSuffix = parts[1].toIntOrNull() ?: return null
        val year = if (yearSuffix < 100) 2000 + yearSuffix else yearSuffix
        return month to year
    }
}
