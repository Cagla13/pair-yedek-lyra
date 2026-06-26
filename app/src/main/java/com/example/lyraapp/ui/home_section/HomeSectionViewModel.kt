package com.example.lyraapp.ui.home_section

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.home.HomeRepository
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
import com.example.lyraapp.data.playlist.PlaylistRepository
import com.example.lyraapp.ui.home.PlayableItem
import com.example.lyraapp.ui.library.LibraryPlaylistItem
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
class HomeSectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val homeRepository: HomeRepository,
    private val playlistRepository: PlaylistRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val section: HomeSection = HomeSection.fromRouteKey(
        savedStateHandle.get<String>(LyraDestination.HomeSection.SECTION_ARG).orEmpty(),
    ) ?: HomeSection.FOR_YOU

    private val _state = MutableStateFlow(
        HomeSectionUiState(title = section.title, isLoading = true),
    )
    val state: StateFlow<HomeSectionUiState> = _state.asStateFlow()

    private val _effect = Channel<HomeSectionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var tracks: List<PlayableItem> = emptyList()

    init {
        loadContent()
    }

    fun onIntent(intent: HomeSectionIntent) {
        when (intent) {
            HomeSectionIntent.Retry -> loadContent()
            is HomeSectionIntent.TrackClicked -> playTrack(intent.trackId)
            is HomeSectionIntent.PlaylistClicked -> viewModelScope.launch {
                _effect.send(HomeSectionEffect.NavigateToPlaylistDetail(intent.playlistId))
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (section) {
                HomeSection.FOR_YOU -> {
                    homeRepository.loadForYou(limit = SEE_ALL_LIMIT)
                        .onSuccess { items -> updateTracks(items) }
                        .onFailure { error -> showError(error) }
                }
                HomeSection.RECOMMENDATIONS -> {
                    homeRepository.loadRecommendations(limit = SEE_ALL_LIMIT)
                        .onSuccess { items -> updateTracks(items) }
                        .onFailure { error -> showError(error) }
                }
                HomeSection.FEATURED_PLAYLISTS -> {
                    playlistRepository.loadPublicPlaylists()
                        .onSuccess { playlists ->
                            _state.update {
                                it.copy(isLoading = false, playlists = playlists)
                            }
                        }
                        .onFailure { error -> showError(error) }
                }
            }
        }
    }

    private fun updateTracks(items: List<PlayableItem>) {
        tracks = items
        _state.update { it.copy(isLoading = false, tracks = items) }
    }

    private fun showError(error: Throwable) {
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = error.message ?: "${section.title} yüklenemedi.",
            )
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
            _effect.send(HomeSectionEffect.NavigateToPlayer)
        }
    }

    private companion object {
        const val SEE_ALL_LIMIT = 50
    }
}

data class HomeSectionUiState(
    val title: String,
    val tracks: List<PlayableItem> = emptyList(),
    val playlists: List<LibraryPlaylistItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface HomeSectionIntent {
    data class TrackClicked(val trackId: String) : HomeSectionIntent
    data class PlaylistClicked(val playlistId: String) : HomeSectionIntent
    data object Retry : HomeSectionIntent
}

sealed interface HomeSectionEffect {
    data object NavigateToPlayer : HomeSectionEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : HomeSectionEffect
}
