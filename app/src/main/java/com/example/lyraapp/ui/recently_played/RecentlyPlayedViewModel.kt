package com.example.lyraapp.ui.recently_played

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
import com.example.lyraapp.data.playlist.PlaylistRepository
import com.example.lyraapp.ui.home.PlayableItem
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
class RecentlyPlayedViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecentlyPlayedUiState(isLoading = true))
    val state: StateFlow<RecentlyPlayedUiState> = _state.asStateFlow()

    private val _effect = Channel<RecentlyPlayedEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var tracks: List<PlayableItem> = emptyList()

    init {
        loadTracks()
    }

    fun onIntent(intent: RecentlyPlayedIntent) {
        when (intent) {
            RecentlyPlayedIntent.Retry -> loadTracks()
            is RecentlyPlayedIntent.TrackClicked -> playTrack(intent.trackId)
            is RecentlyPlayedIntent.TrackLongClicked -> viewModelScope.launch {
                _effect.send(RecentlyPlayedEffect.NavigateToSongDetail(intent.trackId))
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            playlistRepository.loadRecentlyPlayed(limit = 50)
                .onSuccess { items ->
                    tracks = items
                    _state.update { it.copy(isLoading = false, tracks = items) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Son çalınanlar yüklenemedi.",
                        )
                    }
                }
        }
    }

    private fun playTrack(trackId: String) {
        val selected = tracks.find { it.id == trackId } ?: return
        viewModelScope.launch {
            val queue = tracks.map { it.toPlaybackTrack() }
            val index = queue.indexOfFirst { it.id == trackId }.coerceAtLeast(0)
            playerRepository.playTrack(
                track = selected.toPlaybackTrack(),
                queue = queue,
                startIndex = index,
            )
            _effect.send(RecentlyPlayedEffect.NavigateToPlayer)
        }
    }
}

data class RecentlyPlayedUiState(
    val tracks: List<PlayableItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface RecentlyPlayedIntent {
    data class TrackClicked(val trackId: String) : RecentlyPlayedIntent
    data class TrackLongClicked(val trackId: String) : RecentlyPlayedIntent
    data object Retry : RecentlyPlayedIntent
}

sealed interface RecentlyPlayedEffect {
    data object NavigateToPlayer : RecentlyPlayedEffect
    data class NavigateToSongDetail(val songId: String) : RecentlyPlayedEffect
}
