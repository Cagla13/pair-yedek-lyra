package com.example.lyraapp.data.home

import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteHomeRepository @Inject constructor(
    private val api: LyraApiService,
) : HomeRepository {

    override suspend fun loadHomeContent(): Result<HomeContent> = try {
        val forYou = api.getForYou(limit = FOR_YOU_LIMIT).data
        val recentlyPlayed = api.getRecentlyPlayed(limit = RECENTLY_PLAYED_LIMIT).data
        val recommendations = api.getRecommendations(limit = RECOMMENDATIONS_LIMIT).data
        val forYouItems = SongMapper.toPlayableItems(forYou)

        Result.success(
            HomeContent(
                quickPicks = forYouItems.take(QUICK_PICKS_COUNT),
                forYouMusic = forYouItems,
                recentlyPlayed = SongMapper.toPlayableItems(recentlyPlayed),
                recommendations = SongMapper.toPlayableItems(recommendations),
            ),
        )
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    private companion object {
        const val FOR_YOU_LIMIT = 10
        const val QUICK_PICKS_COUNT = 6
        const val RECENTLY_PLAYED_LIMIT = 10
        const val RECOMMENDATIONS_LIMIT = 10
    }
}
