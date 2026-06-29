package com.example.lyraapp.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.playlist.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlayerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        playerRepository.playbackState
            .onEach { playback ->
                val track = playback.track
                _uiState.update {
                    it.copy(
                        title = track?.title.orEmpty(),
                        artist = track?.artist.orEmpty(),
                        sourceTitle = track?.sourceTitle.orEmpty(),
                        currentTrackId = track?.id.orEmpty(),
                        isPlaying = playback.isPlaying,
                        isFavorite = playback.isFavorite,
                        isDownloaded = playback.isDownloaded,
                        isDownloading = playback.isDownloading,
                        progressMs = playback.progressMs,
                        durationMs = playback.durationMs,
                        shuffleEnabled = playback.shuffleEnabled,
                        repeatEnabled = playback.repeatEnabled,
                        isPlayingAd = playback.isPlayingAd,
                        adTitle = playback.adTitle,
                    )
                }
            }
            .launchIn(viewModelScope)

        playerRepository.errorEvents
            .onEach { message ->
                _effect.send(PlayerEffect.ShowErrorMessage(message))
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.Collapse -> viewModelScope.launch {
                _effect.send(PlayerEffect.NavigateBack)
            }
            PlayerIntent.TogglePlayPause -> viewModelScope.launch {
                playerRepository.togglePlayPause()
            }
            PlayerIntent.ToggleFavorite -> viewModelScope.launch {
                playerRepository.toggleFavorite()
            }
            PlayerIntent.ToggleShuffle -> viewModelScope.launch {
                playerRepository.toggleShuffle()
            }
            PlayerIntent.ToggleRepeat -> viewModelScope.launch {
                playerRepository.toggleRepeat()
            }
            PlayerIntent.SkipPrevious -> viewModelScope.launch {
                playerRepository.skipPrevious()
            }
            PlayerIntent.SkipNext -> viewModelScope.launch {
                playerRepository.skipNext()
            }
            PlayerIntent.Download -> viewModelScope.launch {
                playerRepository.downloadTrack()
            }
            PlayerIntent.OpenAddToPlaylist -> openAddToPlaylistDialog()
            PlayerIntent.DismissAddToPlaylist -> {
                _uiState.update { it.copy(showAddToPlaylistDialog = false) }
            }
            is PlayerIntent.AddToPlaylist -> addTrackToPlaylist(intent.playlistId)
            is PlayerIntent.SeekTo -> viewModelScope.launch {
                playerRepository.seekTo(intent.progressMs)
            }
            PlayerIntent.OpenSongDetail -> viewModelScope.launch {
                val trackId = _uiState.value.currentTrackId
                if (trackId.isNotBlank()) {
                    _effect.send(PlayerEffect.NavigateToSongDetail(trackId))
                }
            }
            PlayerIntent.ShowCastInfo -> viewModelScope.launch {
                _effect.send(PlayerEffect.ShowMessage("Cast desteği yakında eklenecek."))
            }
        }
    }

    private fun openAddToPlaylistDialog() {
        val trackId = _uiState.value.currentTrackId
        if (trackId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            playlistRepository.loadUserPlaylists()
                .onSuccess { playlists ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showAddToPlaylistDialog = true,
                            userPlaylists = playlists,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(PlayerEffect.ShowErrorMessage(error.message ?: "Çalma listeleri yüklenemedi."))
                }
        }
    }

    private fun addTrackToPlaylist(playlistId: String) {
        val trackId = _uiState.value.currentTrackId
        if (trackId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToPlaylist = true) }
            playlistRepository.addTrackToPlaylist(playlistId, trackId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isAddingToPlaylist = false,
                            showAddToPlaylistDialog = false,
                        )
                    }
                    _effect.send(PlayerEffect.ShowMessage("Şarkı çalma listesine eklendi."))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isAddingToPlaylist = false) }
                    _effect.send(PlayerEffect.ShowErrorMessage(error.message ?: "Şarkı eklenemedi."))
                }
        }
    }
}
