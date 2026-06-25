package com.example.lyraapp.data.player

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.data.remote.dto.RecordPlayBody
import com.example.lyraapp.service.LyraMediaService
import com.example.lyraapp.ui.favorites.FavoritesStorage
import com.example.lyraapp.ui.favorites.SongUiModel
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

@Singleton
class MediaPlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: ExoPlayer,
    private val api: LyraApiService,
) : PlayerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    override val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

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
        
        startMediaService()
    }

    private fun startMediaService() {
        val intent = Intent(context, LyraMediaService::class.java)
        try {
            context.startService(intent)
        } catch (e: Exception) {
            // Android 12+ kısıtlamaları
        }
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
                if (player.mediaItemCount > 0) {
                    player.play()
                } else {
                    _errorEvents.emit("Oynatılacak kaynak bulunamadı. İnternet bağlantınızı kontrol edin.")
                }
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

    override suspend fun downloadTrack() {
        val track = _playbackState.value.track ?: return
        if (isDownloaded(track.id)) return

        _playbackState.update { it.copy(isDownloading = true) }

        withContext(Dispatchers.IO) {
            try {
                val streamUrl = api.getStreamUrl(track.id).data.url
                val destination = File(context.filesDir, "downloads/${track.id}.mp3")
                destination.parentFile?.mkdirs()

                URL(streamUrl).openStream().use { input ->
                    destination.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                _playbackState.update { it.copy(isDownloading = false, isDownloaded = true) }
            } catch (e: Exception) {
                _playbackState.update { it.copy(isDownloading = false) }
                _errorEvents.emit("İndirme sırasında bir hata oluştu: İnternet bağlantınızı kontrol edin.")
            }
        }
    }

    private fun isDownloaded(trackId: String): Boolean {
        val file = File(context.filesDir, "downloads/$trackId.mp3")
        return file.exists() && file.length() > 0
    }

    private suspend fun startCurrentTrack() {
        val track = queue.getOrNull(currentIndex) ?: return
        
        // 1. Mevcut oynatmayı hemen durdur ve temizle
        withContext(Dispatchers.Main.immediate) {
            player.stop()
            player.clearMediaItems()
            player.seekTo(0)
        }

        // 2. UI State'ini yeni parça bilgileriyle derhal güncelle (Çalmadan önce)
        val isFavorite = FavoritesStorage.savedSongsList.any { it.id == track.id }
        val isLocal = isDownloaded(track.id)
        
        _playbackState.update {
            it.copy(
                track = track,
                isPlaying = false, // Kaynak yüklenene kadar çalma
                isFavorite = isFavorite,
                isDownloaded = isLocal,
                progressMs = 0L,
                durationMs = track.durationMs,
                isVisible = true
            )
        }

        // 3. Kaynak adresini belirle (Yerel veya Uzak)
        val localFile = File(context.filesDir, "downloads/${track.id}.mp3")
        val streamUri = if (isLocal) {
            localFile.absolutePath
        } else {
            try {
                api.getStreamUrl(track.id).data.url
            } catch (e: Exception) {
                null
            }
        }

        // 4. Eğer kaynak bulunamadıysa (offline ve dosya yok) uyar ve dur
        if (streamUri == null) {
            _errorEvents.emit("Bu şarkı indirilmedi, internet bağlantınızı kontrol edin.")
            return
        }

        // 5. Kaynak varsa oynatıcıyı hazırla ve başlat
        withContext(Dispatchers.Main.immediate) {
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setDisplayTitle(track.title)
                .setSubtitle(track.artist)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(streamUri)
                .setMediaId(track.id)
                .setMediaMetadata(mediaMetadata)
                .build()
            
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            startMediaService()
        }

        // 6. Başarılı oynatma durumunda state'i güncelle
        _playbackState.update { it.copy(isPlaying = true) }

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
