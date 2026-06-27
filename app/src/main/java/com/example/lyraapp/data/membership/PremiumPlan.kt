package com.example.lyraapp.data.membership

import com.example.lyraapp.data.remote.dto.MembershipPlanDto

data class PremiumPlan(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val priceLabel: String,
    val monthlyPriceLabel: String? = null,
    val durationDays: Int = 30,
    val isPopular: Boolean,
)

fun MembershipPlanDto.toPremiumPlan(): PremiumPlan {
    val formattedPrice = formatTryPrice(priceKurus.takeIf { it > 0 } ?: price * 100)
    return when (type) {
        "recurring" -> PremiumPlan(
            id = id,
            type = type,
            title = "Aylık abonelik",
            subtitle = "İstediğin zaman iptal et",
            priceLabel = "$formattedPrice / ay",
            monthlyPriceLabel = formattedPrice,
            isPopular = true,
        )
        else -> PremiumPlan(
            id = id,
            type = type,
            title = "Tek seferlik",
            subtitle = "${durationDays} gün erişim · otomatik yenileme yok",
            priceLabel = formattedPrice,
            durationDays = durationDays,
            isPopular = false,
        )
    }
}

fun formatTryPrice(priceKurus: Int): String {
    val lira = priceKurus / 100
    val kurus = priceKurus % 100
    return "₺$lira,${kurus.toString().padStart(2, '0')}"
}
