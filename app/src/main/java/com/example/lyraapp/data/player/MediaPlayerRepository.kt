package com.example.lyraapp.data.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.lyraapp.data.favorites.FavoritesRepository
import com.example.lyraapp.data.favorites.StoredFavorite
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.data.remote.StreamUrlNormalizer
import com.example.lyraapp.data.remote.dto.AdCompleteBody
import com.example.lyraapp.data.remote.dto.PlaybackNextBody
import com.example.lyraapp.service.LyraMediaService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MediaPlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: ExoPlayer,
    private val api: LyraApiService,
    private val favoritesRepository: FavoritesRepository,
) : PlayerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    override val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    private var queue: List<PlaybackTrack> = emptyList()
    private var currentIndex: Int = 0
    private var shuffledOrder: List<Int> = emptyList()
    private var shufflePosition: Int = 0
    private var pendingAdImpressionId: String? = null

    init {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playbackState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        scope.launch { handleTrackEnded() }
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && pendingAdImpressionId != null) {
                        val impressionId = pendingAdImpressionId
                        pendingAdImpressionId = null
                        scope.launch(Dispatchers.IO) {
                            runCatching {
                                api.completeAdPlayback(AdCompleteBody(impressionId!!))
                            }
                        }
                    }
                }
            },
        )
        scope.launch { observeProgress() }
        startMediaService()
    }

    private fun startMediaService() {
        val intent = Intent(context, LyraMediaService::class.java)
        runCatching { context.startService(intent) }
    }

    override suspend fun playTrack(
        track: PlaybackTrack,
        queue: List<PlaybackTrack>,
        startIndex: Int,
    ) {
        this.queue = if (queue.isEmpty()) listOf(track) else queue
        currentIndex = startIndex.coerceIn(0, this.queue.lastIndex)
        rebuildShuffleOrder()
        startCurrentTrack()
    }

    override suspend fun togglePlayPause() {
        withContext(Dispatchers.Main.immediate) {
            if (player.isPlaying) {
                player.pause()
            } else if (player.mediaItemCount > 0) {
                player.play()
            } else if (queue.isNotEmpty()) {
                startCurrentTrack()
            } else {
                _errorEvents.emit("Oynatılacak şarkı bulunamadı.")
            }
        }
    }

    override suspend fun toggleFavorite() {
        val track = _playbackState.value.track ?: return
        val favorite = StoredFavorite(
            id = track.id,
            title = track.title,
            artist = track.artist,
            durationMs = track.durationMs,
        )
        val isFavorite = favoritesRepository.toggle(favorite)
        _playbackState.update { it.copy(isFavorite = isFavorite) }
    }

    override suspend fun toggleShuffle() {
        val enabled = !_playbackState.value.shuffleEnabled
        if (enabled) rebuildShuffleOrder()
        _playbackState.update { it.copy(shuffleEnabled = enabled) }
    }

    override suspend fun toggleRepeat() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        withContext(Dispatchers.Main.immediate) {
            player.repeatMode = when (nextMode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }
        }
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    override suspend fun skipPrevious() {
        if (queue.isEmpty()) return
        if (_playbackState.value.shuffleEnabled && shuffledOrder.size > 1) {
            shufflePosition = if (shufflePosition > 0) shufflePosition - 1 else shuffledOrder.lastIndex
            currentIndex = shuffledOrder[shufflePosition]
        } else {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else queue.lastIndex
        }
        startCurrentTrack()
    }

    override suspend fun skipNext() {
        if (queue.isEmpty()) return
        if (_playbackState.value.shuffleEnabled && shuffledOrder.size > 1) {
            shufflePosition = (shufflePosition + 1) % shuffledOrder.size
            currentIndex = shuffledOrder[shufflePosition]
        } else {
            currentIndex = if (currentIndex < queue.lastIndex) currentIndex + 1 else 0
        }
        startCurrentTrack()
    }

    override suspend fun seekTo(progressMs: Long) {
        withContext(Dispatchers.Main.immediate) {
            player.seekTo(progressMs.coerceAtLeast(0L))
        }
        _playbackState.update { it.copy(progressMs = progressMs) }
    }

    override suspend fun downloadTrack() {
        val track = _playbackState.value.track ?: return
        if (isDownloaded(track.id)) return

        _playbackState.update { it.copy(isDownloading = true) }

        withContext(Dispatchers.IO) {
            try {
                val streamUrl = resolveRemoteStreamUrl(track.id)
                    ?: throw IllegalStateException("Stream URL alınamadı")
                val destination = File(context.filesDir, "downloads/${track.id}.mp3")
                destination.parentFile?.mkdirs()
                URL(streamUrl).openStream().use { input ->
                    destination.outputStream().use { output -> input.copyTo(output) }
                }
                _playbackState.update { it.copy(isDownloading = false, isDownloaded = true) }
            } catch (e: Exception) {
                _playbackState.update { it.copy(isDownloading = false) }
                _errorEvents.emit("İndirme sırasında bir hata oluştu: İnternet bağlantınızı kontrol edin.")
            }
        }
    }

    private suspend fun handleTrackEnded() {
        when (_playbackState.value.repeatMode) {
            RepeatMode.ONE -> {
                withContext(Dispatchers.Main.immediate) {
                    player.seekTo(0)
                    player.play()
                }
            }
            RepeatMode.ALL -> skipNext()
            RepeatMode.OFF -> {
                if (currentIndex < queue.lastIndex || _playbackState.value.shuffleEnabled) {
                    skipNext()
                } else {
                    withContext(Dispatchers.Main.immediate) { player.pause() }
                    _playbackState.update { it.copy(isPlaying = false) }
                }
            }
        }
    }

    private fun rebuildShuffleOrder() {
        if (queue.isEmpty()) {
            shuffledOrder = emptyList()
            shufflePosition = 0
            return
        }
        shuffledOrder = queue.indices.shuffled(Random(System.currentTimeMillis()))
        shufflePosition = shuffledOrder.indexOf(currentIndex).takeIf { it >= 0 } ?: 0
    }

    private fun isDownloaded(trackId: String): Boolean {
        val file = File(context.filesDir, "downloads/$trackId.mp3")
        return file.exists() && file.length() > 0
    }

    private suspend fun startCurrentTrack() {
        val track = queue.getOrNull(currentIndex) ?: return

        withContext(Dispatchers.Main.immediate) {
            player.stop()
            player.clearMediaItems()
            player.seekTo(0)
        }

        val isFavorite = favoritesRepository.isFavorite(track.id)
        val isLocal = isDownloaded(track.id)

        _playbackState.update {
            it.copy(
                track = track,
                isPlaying = false,
                isFavorite = isFavorite,
                isDownloaded = isLocal,
                progressMs = 0L,
                durationMs = track.durationMs,
                isVisible = true,
            )
        }

        val mediaItems = if (isLocal) {
            listOf(
                buildSongMediaItem(
                    track,
                    Uri.fromFile(File(context.filesDir, "downloads/${track.id}.mp3")).toString(),
                ),
            )
        } else {
            resolveRemoteMediaItems(track)
        }

        if (mediaItems.isNullOrEmpty()) {
            _playbackState.update {
                it.copy(track = null, isVisible = false, isPlaying = false)
            }
            _errorEvents.emit("Şarkı oynatılamadı. İnternet bağlantınızı kontrol edin.")
            return
        }

        withContext(Dispatchers.Main.immediate) {
            player.setMediaItems(mediaItems)
            player.repeatMode = when (_playbackState.value.repeatMode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }
            player.prepare()
            player.play()
            startMediaService()
        }

        _playbackState.update { it.copy(isPlaying = true) }
    }

    private suspend fun resolveRemoteMediaItems(track: PlaybackTrack): List<MediaItem>? {
        return runCatching {
            val response = api.playbackNext(PlaybackNextBody(track.id)).data
            when (response.type) {
                "ad" -> {
                    val adUrl = response.adStream?.url?.let(StreamUrlNormalizer::normalize)
                    val songUrl = response.stream?.url?.let(StreamUrlNormalizer::normalize)
                    if (adUrl.isNullOrBlank() || songUrl.isNullOrBlank()) return null
                    pendingAdImpressionId = response.impressionId
                    listOf(
                        buildAdMediaItem(
                            mediaId = response.ad?.id ?: "ad",
                            title = response.ad?.title ?: "Reklam",
                            artist = response.ad?.advertiser ?: "Lyra",
                            uri = adUrl,
                        ),
                        buildSongMediaItem(track, songUrl),
                    )
                }
                else -> {
                    pendingAdImpressionId = null
                    val songUrl = response.stream?.url?.let(StreamUrlNormalizer::normalize)
                        ?: resolvePremiumStreamUrl(track.id)
                    songUrl?.let { listOf(buildSongMediaItem(track, it)) }
                }
            }
        }.getOrElse {
            pendingAdImpressionId = null
            resolvePremiumStreamUrl(track.id)?.let { url ->
                listOf(buildSongMediaItem(track, url))
            }
        }
    }

    private suspend fun resolveRemoteStreamUrl(songId: String): String? {
        return runCatching {
            api.playbackNext(PlaybackNextBody(songId)).data.stream?.url?.let(StreamUrlNormalizer::normalize)
        }.getOrNull() ?: resolvePremiumStreamUrl(songId)
    }

    private suspend fun resolvePremiumStreamUrl(songId: String): String? {
        return runCatching {
            api.getStreamUrl(songId).data.url.let(StreamUrlNormalizer::normalize)
        }.getOrNull()
    }

    private fun buildSongMediaItem(track: PlaybackTrack, uri: String): MediaItem {
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setDisplayTitle(track.title)
            .setSubtitle(track.artist)
            .build()

        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(track.id)
            .setMediaMetadata(mediaMetadata)
            .build()
    }

    private fun buildAdMediaItem(
        mediaId: String,
        title: String,
        artist: String,
        uri: String,
    ): MediaItem {
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setDisplayTitle(title)
            .setSubtitle(artist)
            .build()

        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(mediaId)
            .setMediaMetadata(mediaMetadata)
            .build()
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
