package com.example.lyraapp.ui.player

import androidx.compose.runtime.Immutable

@Immutable
data class PlayerUiState(
    val title: String = "",
    val artist: String = "",
    val sourceTitle: String = "",
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatEnabled: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface PlayerIntent {
    data object Collapse : PlayerIntent
    data object TogglePlayPause : PlayerIntent
    data object ToggleFavorite : PlayerIntent
    data object ToggleShuffle : PlayerIntent
    data object ToggleRepeat : PlayerIntent
    data object SkipPrevious : PlayerIntent
    data object SkipNext : PlayerIntent
    data object Download : PlayerIntent
    data class SeekTo(val progressMs: Long) : PlayerIntent
}

sealed interface PlayerEffect {
    data object NavigateBack : PlayerEffect
    data class ShowErrorMessage(val message: String) : PlayerEffect
}
