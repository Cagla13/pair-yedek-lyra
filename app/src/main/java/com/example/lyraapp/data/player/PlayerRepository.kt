package com.example.lyraapp.data.player

import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val playbackState: StateFlow<PlaybackState>
    suspend fun togglePlayPause()
    suspend fun toggleFavorite()
    suspend fun toggleShuffle()
    suspend fun toggleRepeat()
    suspend fun skipPrevious()
    suspend fun skipNext()
    suspend fun seekTo(progressMs: Long)
}

data class PlaybackTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val sourceTitle: String,
)

data class PlaybackState(
    val track: PlaybackTrack? = null,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatEnabled: Boolean = true,
    val isVisible: Boolean = false,
)
