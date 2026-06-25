package com.example.lyraapp.ui.playlist_detail

class PlaylistDetailContract {

    data class Track(
        val id: String,
        val title: String,
        val artist: String,
        val duration: String,
        val isLiked: Boolean,
        val isPlaying: Boolean,
        val coverColor: Long,
    )

    data class State(
        val playlistTitle: String = "",
        val playlistDescription: String = "",
        val playlistInfo: String = "",
        val tracks: List<Track> = emptyList(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed class Event {
        data object OnPlayClicked : Event()
        data object OnBackClicked : Event()
        data class OnTrackClicked(val trackId: String) : Event()
        data class OnLikeClicked(val trackId: String) : Event()
        data object RetryLoad : Event()
    }
}
