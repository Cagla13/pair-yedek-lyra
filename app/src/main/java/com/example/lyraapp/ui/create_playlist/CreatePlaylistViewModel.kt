
package com.example.lyraapp.ui.create_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.playlist.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePlaylistContract.State())
    val state: StateFlow<CreatePlaylistContract.State> = _state.asStateFlow()

    init {
        loadAvailableTracks()
    }

    fun onEvent(event: CreatePlaylistContract.Event) {
        when (event) {
            is CreatePlaylistContract.Event.OnNameChanged -> {
                _state.update { it.copy(playlistName = event.name) }
            }
            is CreatePlaylistContract.Event.OnDescriptionChanged -> {
                _state.update { it.copy(playlistDescription = event.description) }
            }
            is CreatePlaylistContract.Event.OnPublicToggleChanged -> {
                _state.update { it.copy(isPublic = event.isPublic) }
            }
            is CreatePlaylistContract.Event.OnTrackSelectionToggled -> {
                _state.update { currentState ->
                    val updatedTracks = currentState.availableTracks.map { track ->
                        if (track.id == event.trackId) track.copy(isSelected = !track.isSelected) else track
                    }
                    currentState.copy(availableTracks = updatedTracks)
                }
            }
            CreatePlaylistContract.Event.OnSaveClicked -> savePlaylist()
            CreatePlaylistContract.Event.OnCloseClicked -> Unit
            CreatePlaylistContract.Event.RetryLoadTracks -> loadAvailableTracks()
        }
    }

    private fun loadAvailableTracks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTracks = true, errorMessage = null) }
            playlistRepository.loadSelectableSongs()
                .onSuccess { tracks ->
                    _state.update {
                        it.copy(
                            isLoadingTracks = false,
                            availableTracks = tracks.map { track ->
                                CreatePlaylistContract.SelectableTrack(
                                    id = track.id,
                                    title = track.title,
                                    artist = track.artist,
                                    coverColor = track.coverColor,
                                )
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingTracks = false,
                            errorMessage = error.message ?: "Şarkılar yüklenemedi.",
                        )
                    }
                }
        }
    }

    private fun savePlaylist() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.playlistName.isBlank() || currentState.isSaving) return@launch

            _state.update { it.copy(isSaving = true, errorMessage = null) }
            playlistRepository.createNewPlaylistWithTracks(
                name = currentState.playlistName,
                description = currentState.playlistDescription,
                songIds = currentState.selectedTrackIds,
            ).onSuccess {
                _state.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Çalma listesi oluşturulamadı.",
                    )
                }
            }
        }
    }
}
