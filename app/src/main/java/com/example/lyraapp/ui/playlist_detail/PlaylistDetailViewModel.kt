package com.example.lyraapp.ui.playlist_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.data.favorites.FavoritesRepository
import com.example.lyraapp.data.favorites.StoredFavorite
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
import com.example.lyraapp.data.playlist.PlaylistRepository
import com.example.lyraapp.ui.navigation.LyraDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playerRepository: PlayerRepository,
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val playlistId: String =
        savedStateHandle.get<String>(LyraDestination.PlaylistDetail.PLAYLIST_ID_ARG).orEmpty()

    private val _state = MutableStateFlow(PlaylistDetailContract.State())
    val state: StateFlow<PlaylistDetailContract.State> = _state.asStateFlow()

    private val _navigateToPlayer = Channel<Unit>(Channel.BUFFERED)
    val navigateToPlayer = _navigateToPlayer.receiveAsFlow()

    private var playbackTracks: List<com.example.lyraapp.data.playlist.PlaylistDetailTrack> = emptyList()
    private var isEditable: Boolean = false

    init {
        loadPlaylist()
    }

    fun onEvent(event: PlaylistDetailContract.Event) {
        when (event) {
            PlaylistDetailContract.Event.OnPlayClicked -> playFromIndex(0)
            PlaylistDetailContract.Event.OnBackClicked -> Unit
            PlaylistDetailContract.Event.OnRefreshClicked -> loadPlaylist()
            is PlaylistDetailContract.Event.OnTrackClicked -> playFromTrackId(event.trackId)
            is PlaylistDetailContract.Event.OnLikeClicked -> toggleLike(event.trackId)
            is PlaylistDetailContract.Event.OnRemoveTrackClicked -> removeTrack(event.trackId)
            PlaylistDetailContract.Event.RetryLoad -> loadPlaylist()
        }
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val currentUserId = authRepository.currentUser.first()?.id
            val favorites = favoritesRepository.getFavorites().map { it.id }.toSet()
            playlistRepository.getPlaylistDetail(playlistId)
                .onSuccess { content ->
                    playbackTracks = content.tracks
                    isEditable = !content.ownerId.isNullOrBlank() && content.ownerId == currentUserId
                    _state.value = PlaylistDetailContract.State(
                        playlistTitle = content.title,
                        playlistDescription = content.description,
                        playlistInfo = content.info,
                        isEditable = isEditable,
                        tracks = content.tracks.map { track ->
                            PlaylistDetailContract.Track(
                                id = track.id,
                                title = track.title,
                                artist = track.artist,
                                duration = track.duration,
                                isLiked = track.id in favorites,
                                isPlaying = false,
                                coverColor = track.coverColor,
                            )
                        },
                        isLoading = false,
                    )
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Çalma listesi yüklenemedi.",
                        )
                    }
                }
        }
    }

    private fun removeTrack(trackId: String) {
        if (!isEditable) return
        viewModelScope.launch {
            playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
                .onSuccess { loadPlaylist() }
                .onFailure { error ->
                    _state.update {
                        it.copy(errorMessage = error.message ?: "Şarkı listeden kaldırılamadı.")
                    }
                }
        }
    }

    private fun playFromTrackId(trackId: String) {
        val index = playbackTracks.indexOfFirst { it.id == trackId }.takeIf { it >= 0 } ?: return
        playFromIndex(index)
    }

    private fun playFromIndex(index: Int) {
        if (playbackTracks.isEmpty()) return
        viewModelScope.launch {
            val sourceTitle = _state.value.playlistTitle
            val queue = playbackTracks.map { it.toPlaybackTrack(sourceTitle) }
            playerRepository.playTrack(
                track = queue[index],
                queue = queue,
                startIndex = index,
            )
            _state.update { current ->
                current.copy(
                    tracks = current.tracks.map { track ->
                        track.copy(isPlaying = track.id == queue[index].id)
                    },
                )
            }
            _navigateToPlayer.send(Unit)
        }
    }

    private fun toggleLike(trackId: String) {
        val track = _state.value.tracks.find { it.id == trackId } ?: return
        viewModelScope.launch {
            val parts = track.duration.split(":")
            val durationMs = if (parts.size == 2) {
                ((parts[0].toLongOrNull() ?: 0L) * 60 + (parts[1].toLongOrNull() ?: 0L)) * 1000
            } else {
                0L
            }
            val isLiked = favoritesRepository.toggle(
                StoredFavorite(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    durationMs = durationMs,
                ),
            )
            _state.update { current ->
                current.copy(
                    tracks = current.tracks.map {
                        if (it.id == trackId) it.copy(isLiked = isLiked) else it
                    },
                )
            }
        }
    }
}
