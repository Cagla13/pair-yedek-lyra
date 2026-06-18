package com.example.lyraapp.data.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePlayerRepository @Inject constructor() : PlayerRepository {

    private val demoTrack = PlaybackTrack(
        id = "neon-sokaklar",
        title = "Neon Sokaklar",
        artist = "Şehir Işıkları",
        album = "Gece Vardiyası",
        sourceTitle = "Gece Vardiyası",
    )

    private val _playbackState = MutableStateFlow(
        PlaybackState(
            track = demoTrack,
            isPlaying = true,
            isFavorite = true,
            progressMs = 93_000L,
            durationMs = 223_000L,
            shuffleEnabled = false,
            repeatEnabled = true,
            isVisible = true,
        )
    )

    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    override suspend fun togglePlayPause() {
        _playbackState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    override suspend fun toggleFavorite() {
        _playbackState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    override suspend fun toggleShuffle() {
        _playbackState.update { it.copy(shuffleEnabled = !it.shuffleEnabled) }
    }

    override suspend fun toggleRepeat() {
        _playbackState.update { it.copy(repeatEnabled = !it.repeatEnabled) }
    }

    override suspend fun skipPrevious() = Unit

    override suspend fun skipNext() = Unit

    override suspend fun seekTo(progressMs: Long) {
        _playbackState.update { state ->
            state.copy(progressMs = progressMs.coerceIn(0L, state.durationMs))
        }
    }
}
