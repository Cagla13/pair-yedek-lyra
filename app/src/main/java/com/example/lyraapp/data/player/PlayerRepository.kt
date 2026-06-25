package com.example.lyraapp.data.player

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val playbackState: StateFlow<PlaybackState>
    val errorEvents: SharedFlow<String>

    suspend fun playTrack(
        track: PlaybackTrack,
        queue: List<PlaybackTrack> = listOf(track),
        startIndex: Int = 0,
    )

    suspend fun togglePlayPause()
    suspend fun toggleFavorite()
    suspend fun toggleShuffle()
    suspend fun toggleRepeat()
    suspend fun skipPrevious()
    suspend fun skipNext()
    suspend fun seekTo(progressMs: Long)
    suspend fun downloadTrack()
}

data class PlaybackTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val sourceTitle: String,
    val durationMs: Long = 0L,
    val localUri: String? = null,
) {
    fun durationLabel(): String {
        val seconds = durationMs / 1000
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }
}

data class PlaybackState(
    val track: PlaybackTrack? = null,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatEnabled: Boolean = true,
    val isVisible: Boolean = false,
)
