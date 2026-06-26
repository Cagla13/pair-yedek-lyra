package com.example.lyraapp.ui.player

import androidx.compose.runtime.Immutable
import com.example.lyraapp.data.playlist.UserPlaylistOption

@Immutable
data class PlayerUiState(
    val title: String = "",
    val artist: String = "",
    val sourceTitle: String = "",
    val currentTrackId: String = "",
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val userPlaylists: List<UserPlaylistOption> = emptyList(),
    val isAddingToPlaylist: Boolean = false,
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
    data object OpenAddToPlaylist : PlayerIntent
    data object DismissAddToPlaylist : PlayerIntent
    data object OpenSongDetail : PlayerIntent
    data object ShowCastInfo : PlayerIntent
    data class AddToPlaylist(val playlistId: String) : PlayerIntent
    data class SeekTo(val progressMs: Long) : PlayerIntent
}

sealed interface PlayerEffect {
    data object NavigateBack : PlayerEffect
    data class NavigateToSongDetail(val songId: String) : PlayerEffect
    data class ShowErrorMessage(val message: String) : PlayerEffect
    data class ShowMessage(val message: String) : PlayerEffect
}
