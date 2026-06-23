package com.example.lyraapp.data.home

import com.example.lyraapp.ui.home.PlayableItem

data class HomeContent(
    val quickPicks: List<PlayableItem> = emptyList(),
    val recentlyPlayed: List<PlayableItem> = emptyList(),
    val recommendations: List<PlayableItem> = emptyList(),
)

interface HomeRepository {
    suspend fun loadHomeContent(): Result<HomeContent>
}
