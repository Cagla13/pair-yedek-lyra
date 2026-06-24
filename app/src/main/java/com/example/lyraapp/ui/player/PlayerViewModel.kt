package com.example.lyraapp.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.player.PlayerRepository
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
                        isPlaying = playback.isPlaying,
                        isFavorite = playback.isFavorite,
                        progressMs = playback.progressMs,
                        durationMs = playback.durationMs,
                        shuffleEnabled = playback.shuffleEnabled,
                        repeatEnabled = playback.repeatEnabled,
                    )
                }
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
            is PlayerIntent.SeekTo -> viewModelScope.launch {
                playerRepository.seekTo(intent.progressMs)
            }
        }
    }
}