package com.example.lyraapp.ui.premium

import androidx.compose.runtime.Immutable

enum class PremiumPlanType(val apiValue: String) {
    RECURRING("recurring"),
    ONE_TIME("one-time"),
    ;

    companion object {
        fun fromApiValue(value: String?): PremiumPlanType? =
            entries.firstOrNull { it.apiValue == value }
    }
}

@Immutable
data class PremiumFeatureUi(
    val title: String,
    val subtitle: String,
)

@Immutable
data class PremiumPlanUi(
    val id: String,
    val type: PremiumPlanType,
    val title: String,
    val subtitle: String,
    val priceLabel: String,
    val isPopular: Boolean,
)

@Immutable
data class PremiumUiState(
    val isLoading: Boolean = true,
    val features: List<PremiumFeatureUi> = defaultFeatures,
    val plans: List<PremiumPlanUi> = emptyList(),
    val selectedPlanType: PremiumPlanType = PremiumPlanType.RECURRING,
    val footerNote: String = "",
)

@Immutable
data class PremiumExpiryPromptUi(
    val daysRemaining: Int,
    val recurringPriceLabel: String,
    val oneTimePriceLabel: String,
    val oneTimeDurationDays: Int = 30,
)

val defaultFeatures = listOf(
    PremiumFeatureUi("Reklamsız dinleme", "Kesintisiz, sınırsız müzik"),
    PremiumFeatureUi("Sınırsız atlama", "İstediğin şarkıya geç"),
    PremiumFeatureUi("Çevrimdışı indirme", "İnternet olmadan dinle"),
    PremiumFeatureUi("Yüksek ses kalitesi", "320 kbps net ses"),
    PremiumFeatureUi("Tüm cihazlarında", "Telefon, tablet ve masaüstü"),
)

sealed interface PremiumIntent {
    data class SelectPlan(val planType: PremiumPlanType) : PremiumIntent
    data object Continue : PremiumIntent
    data object Back : PremiumIntent
}

sealed interface PremiumEffect {
    data object NavigateBack : PremiumEffect
    data class ShowMessage(val message: String) : PremiumEffect
}

sealed interface PremiumExpiryIntent {
    data object Dismiss : PremiumExpiryIntent
    data object ChooseRecurring : PremiumExpiryIntent
    data object ChooseOneTime : PremiumExpiryIntent
}
