package com.example.lyraapp.data.membership

interface MembershipRepository {
    suspend fun loadPlans(): Result<List<PremiumPlan>>
    suspend fun checkout(planType: String, card: CheckoutCardDetails): Result<Unit>
}
