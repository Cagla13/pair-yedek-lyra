package com.example.lyraapp.data.membership

import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
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
}
