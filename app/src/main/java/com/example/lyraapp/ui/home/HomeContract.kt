package com.example.lyraapp.ui.home

import androidx.compose.runtime.Immutable

@Immutable
data class HomeUiState(
    val userName: String = "",
    val userInitials: String = "",
    val quickPicks: List<PlayableItem> = emptyList(),
    val recentlyPlayed: List<PlayableItem> = emptyList(),
    val customPlaylists: List<PlayableItem> = emptyList(),
    val currentPlayingTrack: PlayableItem? = null,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class PlayableItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val gradientIndex: Int = 0,
    val durationMs: Long = 0L,
)

sealed interface HomeIntent {
    data class QuickPickClicked(val itemId: String) : HomeIntent
    data class TrackClicked(val itemId: String) : HomeIntent
    data object TogglePlayPause : HomeIntent
    data object ToggleFavorite : HomeIntent
    data object ProfileClicked : HomeIntent
    data object SeeAllRecentlyPlayedClicked : HomeIntent
    data object MiniPlayerClicked : HomeIntent
    data object RetryLoad : HomeIntent
}

sealed interface HomeEffect {
    data object NavigateToProfile : HomeEffect
    data object NavigateToPlayer : HomeEffect
    data class ShowNotification(val message: String) : HomeEffect
}