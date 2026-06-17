package com.example.lyraapp.ui.home

import androidx.compose.runtime.Immutable

@Immutable
data class HomeUiState(
    val userName: String = "",
    val quickPicks: List<PlayableItem> = emptyList(),
    val recentlyPlayed: List<PlayableItem> = emptyList(),
    val customPlaylists: List<PlayableItem> = emptyList(),
    val currentPlayingTrack: PlayableItem? = null,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false
)

data class PlayableItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val gradientIndex: Int = 0
)

sealed interface HomeIntent {
    data class QuickPickClicked(val itemId: String) : HomeIntent
    data class TrackClicked(val itemId: String) : HomeIntent
    data object TogglePlayPause : HomeIntent
    data object ToggleFavorite : HomeIntent
    data object ProfileClicked : HomeIntent
    data object SeeAllRecentlyPlayedClicked : HomeIntent
}

sealed interface HomeEffect {
    data class NavigateToDetails(val itemId: String) : HomeEffect
    data object NavigateToProfile : HomeEffect
    data class ShowNotification(val message: String) : HomeEffect
}