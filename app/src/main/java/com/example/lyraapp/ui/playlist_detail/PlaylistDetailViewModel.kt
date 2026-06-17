package com.example.lyraapp.ui.playlist_detail

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PlaylistDetailContract.State())
    val state: StateFlow<PlaylistDetailContract.State> = _state.asStateFlow()

    fun onEvent(event: PlaylistDetailContract.Event) {
        when (event) {
            is PlaylistDetailContract.Event.OnPlayClicked -> { }
            is PlaylistDetailContract.Event.OnBackClicked -> { }
            is PlaylistDetailContract.Event.OnTrackClicked -> {
                _state.update { currentState ->
                    val updatedTracks = currentState.tracks.map { track ->
                        track.copy(isPlaying = track.id == event.trackId)
                    }
                    currentState.copy(tracks = updatedTracks)
                }
            }
            is PlaylistDetailContract.Event.OnLikeClicked -> {
                _state.update { currentState ->
                    val updatedTracks = currentState.tracks.map { track ->
                        if (track.id == event.trackId) track.copy(isLiked = !track.isLiked) else track
                    }
                    currentState.copy(tracks = updatedTracks)
                }
            }
        }
    }
}