package com.example.lyraapp.data.membership

import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.data.remote.dto.CheckoutCardBody
import com.example.lyraapp.data.remote.dto.CheckoutRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteMembershipRepository @Inject constructor(
    private val api: LyraApiService,
) : MembershipRepository {

    override suspend fun loadPlans(): Result<List<PremiumPlan>> = try {
        val plans = api.getMembershipPlans()
            .data
            .map { it.toPremiumPlan() }
            .sortedByDescending { it.isPopular }
        Result.success(plans)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    override suspend fun checkout(planType: String, card: CheckoutCardDetails): Result<Unit> = try {
        api.checkout(
            CheckoutRequestBody(
                plan = planType,
                card = CheckoutCardBody(
                    number = card.number,
                    expMonth = card.expMonth,
                    expYear = card.expYear,
                    cvc = card.cvc,
                    holderName = card.holderName.takeIf { it.isNotBlank() },
                ),
            ),
        )
        Result.success(Unit)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }
}
