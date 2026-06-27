package com.example.lyraapp.ui.home

import androidx.compose.runtime.Immutable

import com.example.lyraapp.ui.premium.PremiumExpiryPromptUi

@Immutable
data class HomeUiState(
    val userName: String = "",
    val userInitials: String = "",
    val quickPicks: List<PlayableItem> = emptyList(),
    val forYouMusic: List<PlayableItem> = emptyList(),
    val recentlyPlayed: List<PlayableItem> = emptyList(),
    val recommendations: List<PlayableItem> = emptyList(),
    val featuredPlaylists: List<com.example.lyraapp.ui.library.LibraryPlaylistItem> = emptyList(),
    val currentPlayingTrack: PlayableItem? = null,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val premiumExpiryPrompt: PremiumExpiryPromptUi? = null,
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
    data class FeaturedPlaylistClicked(val playlistId: String) : HomeIntent
    data object SeeAllRecentlyPlayedClicked : HomeIntent
    data object SeeAllForYouClicked : HomeIntent
    data object SeeAllRecommendationsClicked : HomeIntent
    data object SeeAllFeaturedPlaylistsClicked : HomeIntent
    data object MiniPlayerClicked : HomeIntent
    data object RetryLoad : HomeIntent
    data object DismissPremiumExpiryPrompt : HomeIntent
    data object PremiumExpiryChooseRecurring : HomeIntent
    data object PremiumExpiryChooseOneTime : HomeIntent
}

sealed interface HomeEffect {
    data object NavigateToProfile : HomeEffect
    data object NavigateToPlayer : HomeEffect
    data object NavigateToRecentlyPlayed : HomeEffect
    data object NavigateToForYou : HomeEffect
    data object NavigateToRecommendations : HomeEffect
    data object NavigateToFeaturedPlaylists : HomeEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : HomeEffect
    data class ShowNotification(val message: String) : HomeEffect
    data class NavigateToPremium(val planType: String? = null) : HomeEffect
}