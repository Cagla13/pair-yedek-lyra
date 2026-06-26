package com.example.lyraapp.ui.song_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.home.formatDurationMs
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
import com.example.lyraapp.data.playlist.PlaylistRepository
import com.example.lyraapp.ui.navigation.LyraDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val songId: String =
        savedStateHandle.get<String>(LyraDestination.SongDetail.SONG_ID_ARG).orEmpty()

    private val _state = MutableStateFlow(SongDetailUiState(isLoading = true))
    val state: StateFlow<SongDetailUiState> = _state.asStateFlow()

    private val _effect = Channel<SongDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadSong()
    }

    fun onIntent(intent: SongDetailIntent) {
        when (intent) {
            SongDetailIntent.Retry -> loadSong()
            SongDetailIntent.Play -> playSong()
        }
    }

    private fun loadSong() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            playlistRepository.getSongDetail(songId)
                .onSuccess { song ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            title = song.title,
                            artist = song.artist,
                            album = song.album.orEmpty(),
                            duration = formatDurationMs(song.durationMs),
                            mimeType = song.mimeType.orEmpty(),
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Şarkı detayı yüklenemedi.",
                        )
                    }
                }
        }
    }

    private fun playSong() {
        viewModelScope.launch {
            playlistRepository.getSongDetail(songId)
                .onSuccess { song ->
                    val track = song.toPlaybackTrack()
                    playerRepository.playTrack(track = track, queue = listOf(track))
                    _effect.send(SongDetailEffect.NavigateToPlayer)
                }
                .onFailure { error ->
                    _effect.send(SongDetailEffect.ShowError(error.message ?: "Şarkı oynatılamadı."))
                }
        }
    }
}

data class SongDetailUiState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val duration: String = "",
    val mimeType: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SongDetailIntent {
    data object Play : SongDetailIntent
    data object Retry : SongDetailIntent
}

sealed interface SongDetailEffect {
    data object NavigateToPlayer : SongDetailEffect
    data class ShowError(val message: String) : SongDetailEffect
}
