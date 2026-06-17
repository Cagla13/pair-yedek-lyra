package com.example.lyraapp.ui.favorites

data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val imageUrl: String? = null,
    val isPlaying: Boolean = false
)

class FavoritesContract {
    data class State(
        val songs: List<SongUiModel> = emptyList(),
        val totalSongsCount: Int = 0,
        val totalDurationText: String = ""
    )

    sealed interface Intent {
        data class OnSongClick(val songId: String) : Intent
        data class OnRemoveFromFavorites(val songId: String) : Intent
        data object OnPlayAllClick : Intent
        data object OnShuffleClick : Intent
    }

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}