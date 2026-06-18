package com.example.lyraapp.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackBarViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playerRepository.playbackState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaybackState(),
        )

    fun togglePlayPause() {
        viewModelScope.launch { playerRepository.togglePlayPause() }
    }
}
