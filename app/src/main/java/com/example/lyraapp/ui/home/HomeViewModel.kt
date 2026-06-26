package com.example.lyraapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.data.home.HomeRepository
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val homeRepository: HomeRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private var cachedTracks: List<PlayableItem> = emptyList()
    private var skipNextHomeResumeRefresh = true

    init {
        observeCurrentUser()
        observePlayback()
        loadHomeContent()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        userName = user?.fullName.orEmpty(),
                        userInitials = user?.initials.orEmpty(),
                    )
                }
            }
        }
    }

    private fun observePlayback() {
        playerRepository.playbackState
            .onEach { playback ->
                val track = playback.track
                _uiState.update { current ->
                    current.copy(
                        currentPlayingTrack = track?.let { playbackTrack ->
                            PlayableItem(
                                id = playbackTrack.id,
                                title = playbackTrack.title,
                                subtitle = playbackTrack.artist,
                                durationMs = playbackTrack.durationMs,
                            )
                        },
                        isPlaying = playback.isPlaying,
                        isFavorite = playback.isFavorite,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.QuickPickClicked -> playTrack(intent.itemId)
            is HomeIntent.TrackClicked -> playTrack(intent.itemId)
            HomeIntent.TogglePlayPause -> viewModelScope.launch {
                playerRepository.togglePlayPause()
            }
            HomeIntent.ToggleFavorite -> viewModelScope.launch {
                playerRepository.toggleFavorite()
            }
            HomeIntent.ProfileClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToProfile)
            }
            HomeIntent.SeeAllRecentlyPlayedClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToRecentlyPlayed)
            }
            HomeIntent.SeeAllForYouClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToForYou)
            }
            HomeIntent.SeeAllRecommendationsClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToRecommendations)
            }
            HomeIntent.SeeAllFeaturedPlaylistsClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToFeaturedPlaylists)
            }
            is HomeIntent.FeaturedPlaylistClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToPlaylistDetail(intent.playlistId))
            }
            HomeIntent.MiniPlayerClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToPlayer)
            }
            HomeIntent.RetryLoad -> loadHomeContent()
        }
    }

    fun onHomeResumed() {
        if (skipNextHomeResumeRefresh) {
            skipNextHomeResumeRefresh = false
            return
        }
        refreshRecentlyPlayed()
    }

    private fun playTrack(itemId: String) {
        val selected = cachedTracks.find { it.id == itemId } ?: return
        viewModelScope.launch {
            val queue = cachedTracks.map { it.toPlaybackTrack() }
            val index = queue.indexOfFirst { it.id == itemId }.coerceAtLeast(0)
            playerRepository.playTrack(
                track = selected.toPlaybackTrack(),
                queue = queue,
                startIndex = index,
            )
            _effect.send(HomeEffect.NavigateToPlayer)
        }
    }

    private fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            homeRepository.loadHomeContent()
                .onSuccess { content ->
                    cachedTracks = buildList {
                        addAll(content.forYouMusic)
                        addAll(content.recentlyPlayed)
                        addAll(content.recommendations)
                    }.distinctBy { it.id }

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            quickPicks = content.quickPicks,
                            forYouMusic = content.forYouMusic,
                            recentlyPlayed = content.recentlyPlayed,
                            recommendations = content.recommendations,
                            featuredPlaylists = content.featuredPlaylists,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Ana sayfa yüklenemedi.",
                        )
                    }
                    _effect.send(
                        HomeEffect.ShowNotification(
                            error.message ?: "Ana sayfa yüklenemedi.",
                        ),
                    )
                }
        }
    }

    private fun refreshRecentlyPlayed() {
        viewModelScope.launch {
            homeRepository.loadRecentlyPlayed(limit = RECENTLY_PLAYED_LIMIT)
                .onSuccess { items ->
                    cachedTracks = buildList {
                        addAll(_uiState.value.forYouMusic)
                        addAll(items)
                        addAll(_uiState.value.recommendations)
                    }.distinctBy { it.id }

                    _uiState.update { it.copy(recentlyPlayed = items) }
                }
        }
    }

    private companion object {
        const val RECENTLY_PLAYED_LIMIT = 10
    }
}
