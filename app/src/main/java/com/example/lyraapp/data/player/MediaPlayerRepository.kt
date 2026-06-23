package com.example.lyraapp.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.data.remote.dto.RecordPlayBody
import com.example.lyraapp.ui.favorites.FavoritesStorage
import com.example.lyraapp.ui.favorites.SongUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlayerRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val api: LyraApiService,
) : PlayerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var queue: List<PlaybackTrack> = emptyList()
    private var currentIndex: Int = 0

    init {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playbackState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        scope.launch { skipNext() }
                    }
                }
            },
        )
        scope.launch { observeProgress() }
    }

    override suspend fun playTrack(
        track: PlaybackTrack,
        queue: List<PlaybackTrack>,
        startIndex: Int,
    ) {
        this.queue = if (queue.isEmpty()) listOf(track) else queue
        currentIndex = startIndex.coerceIn(0, this.queue.lastIndex)
        startCurrentTrack()
    }

    override suspend fun togglePlayPause() {
        withContext(Dispatchers.Main.immediate) {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    override suspend fun toggleFavorite() {
        val track = _playbackState.value.track ?: return
        val isCurrentlyFav = FavoritesStorage.savedSongsList.any { it.id == track.id }
        if (isCurrentlyFav) {
            FavoritesStorage.savedSongsList.removeAll { it.id == track.id }
        } else {
            FavoritesStorage.savedSongsList.add(
                SongUiModel(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    duration = track.durationLabel(),
                    isPlaying = _playbackState.value.isPlaying,
                ),
            )
        }
        _playbackState.update { it.copy(isFavorite = !isCurrentlyFav) }
    }

    override suspend fun toggleShuffle() {
        _playbackState.update { it.copy(shuffleEnabled = !it.shuffleEnabled) }
    }

    override suspend fun toggleRepeat() {
        _playbackState.update { it.copy(repeatEnabled = !it.repeatEnabled) }
    }

    override suspend fun skipPrevious() {
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else queue.lastIndex
        startCurrentTrack()
    }

    override suspend fun skipNext() {
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex < queue.lastIndex) currentIndex + 1 else 0
        startCurrentTrack()
    }

    override suspend fun seekTo(progressMs: Long) {
        withContext(Dispatchers.Main.immediate) {
            player.seekTo(progressMs.coerceAtLeast(0L))
        }
        _playbackState.update { it.copy(progressMs = progressMs) }
    }

    private suspend fun startCurrentTrack() {
        val track = queue.getOrNull(currentIndex) ?: return
        val streamUrl = api.getStreamUrl(track.id).data.url
        withContext(Dispatchers.Main.immediate) {
            player.setMediaItem(MediaItem.fromUri(streamUrl))
            player.prepare()
            player.play()
        }
        val isFavorite = FavoritesStorage.savedSongsList.any { it.id == track.id }
        _playbackState.value = PlaybackState(
            track = track,
            isPlaying = true,
            isFavorite = isFavorite,
            progressMs = 0L,
            durationMs = track.durationMs,
            shuffleEnabled = _playbackState.value.shuffleEnabled,
            repeatEnabled = _playbackState.value.repeatEnabled,
            isVisible = true,
        )
        scope.launch(Dispatchers.IO) {
            runCatching { api.recordPlay(RecordPlayBody(track.id)) }
        }
    }

    private suspend fun observeProgress() {
        while (true) {
            delay(500)
            val duration = player.duration.takeIf { it > 0 } ?: _playbackState.value.durationMs
            _playbackState.update {
                it.copy(
                    progressMs = player.currentPosition.coerceAtLeast(0L),
                    durationMs = duration,
                    isPlaying = player.isPlaying,
                )
            }
        }
    }
}
