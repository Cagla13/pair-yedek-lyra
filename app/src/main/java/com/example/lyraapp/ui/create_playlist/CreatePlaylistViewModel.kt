package com.example.lyraapp.ui.create_playlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CreatePlaylistContract.State())
    val state: StateFlow<CreatePlaylistContract.State> = _state.asStateFlow()

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
            CreatePlaylistContract.Event.OnSaveClicked -> {

            }
            CreatePlaylistContract.Event.OnCloseClicked -> {

            }
        }
    }
}