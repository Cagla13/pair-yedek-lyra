package com.example.lyraapp.ui.premium

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.membership.MembershipRepository
import com.example.lyraapp.ui.navigation.LyraDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val membershipRepository: MembershipRepository,
) : ViewModel() {

    private val initialPlan = PremiumPlanType.fromApiValue(
        savedStateHandle.get<String>(LyraDestination.Premium.PLAN_ARG),
    ) ?: PremiumPlanType.RECURRING

    private val _uiState = MutableStateFlow(PremiumUiState(selectedPlanType = initialPlan))
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PremiumEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadPlans()
    }

    fun onIntent(intent: PremiumIntent) {
        when (intent) {
            is PremiumIntent.SelectPlan -> {
                _uiState.update { it.copy(selectedPlanType = intent.planType) }
            }
            PremiumIntent.Continue -> viewModelScope.launch {
                val state = _uiState.value
                val selectedPlan = state.plans.find { it.type == state.selectedPlanType }
                if (selectedPlan != null) {
                    _effect.send(
                        PremiumEffect.NavigateToPayment(
                            price = selectedPlan.priceLabel,
                            title = "LyraApp Premium",
                            desc = selectedPlan.title
                        )
                    )
                } else {
                    _effect.send(PremiumEffect.ShowMessage("Lütfen bir plan seçin."))
                }
            }
            PremiumIntent.Back -> viewModelScope.launch {
                _effect.send(PremiumEffect.NavigateBack)
            }
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            membershipRepository.loadPlans()
                .onSuccess { plans ->
                    val planUi = plans.mapNotNull { plan ->
                        val type = PremiumPlanType.fromApiValue(plan.type) ?: return@mapNotNull null
                        PremiumPlanUi(
                            id = plan.id,
                            type = type,
                            title = plan.title,
                            subtitle = plan.subtitle,
                            priceLabel = plan.priceLabel,
                            isPopular = plan.isPopular,
                        )
                    }
                    val recurring = plans.firstOrNull { it.type == PremiumPlanType.RECURRING.apiValue }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plans = planUi,
                            selectedPlanType = planUi.firstOrNull { plan -> plan.type == initialPlan }?.type
                                ?: planUi.firstOrNull()?.type
                                ?: PremiumPlanType.RECURRING,
                            footerNote = recurring?.monthlyPriceLabel?.let { price ->
                                "Aylık $price. Dilediğin zaman iptal edebilirsin."
                            }.orEmpty(),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(PremiumEffect.ShowMessage(error.message ?: "Planlar yüklenemedi."))
                }
        }
    }
}
