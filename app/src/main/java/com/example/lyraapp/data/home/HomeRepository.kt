package com.example.lyraapp.data.home

import com.example.lyraapp.ui.home.PlayableItem
import com.example.lyraapp.ui.library.LibraryPlaylistItem

data class HomeContent(
    val quickPicks: List<PlayableItem> = emptyList(),
    val forYouMusic: List<PlayableItem> = emptyList(),
    val recentlyPlayed: List<PlayableItem> = emptyList(),
    val recommendations: List<PlayableItem> = emptyList(),
    val featuredPlaylists: List<LibraryPlaylistItem> = emptyList(),
)

interface HomeRepository {
    suspend fun loadHomeContent(): Result<HomeContent>

    suspend fun loadRecentlyPlayed(limit: Int = 10): Result<List<PlayableItem>>

    suspend fun loadForYou(limit: Int = 50): Result<List<PlayableItem>>

    suspend fun loadRecommendations(limit: Int = 50): Result<List<PlayableItem>>
}
