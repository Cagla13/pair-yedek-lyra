package com.example.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.library.LibraryRepository
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
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState(isLoading = true))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadPlaylists()
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = intent.filter) }
            }
            LibraryIntent.ToggleViewMode -> {
                _uiState.update { it.copy(isGridView = !it.isGridView) }
            }
            LibraryIntent.SearchClicked -> sendEffect(LibraryEffect.NavigateToSearch)
            LibraryIntent.CreatePlaylistClicked -> sendEffect(LibraryEffect.NavigateToCreatePlaylist)
            is LibraryIntent.PlaylistClicked -> sendEffect(
                LibraryEffect.NavigateToPlaylistDetail(intent.playlistId),
            )
            is LibraryIntent.PlaylistMenuClicked -> sendEffect(
                LibraryEffect.ShowMessage("Çalma listesi seçenekleri yakında eklenecek."),
            )
            LibraryIntent.RetryLoad -> loadPlaylists()
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            libraryRepository.loadPlaylists()
                .onSuccess { playlists ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            playlists = playlists,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Kütüphane yüklenemedi.",
                        )
                    }
                    sendEffect(LibraryEffect.ShowMessage(error.message ?: "Kütüphane yüklenemedi."))
                }
        }
    }

    private fun sendEffect(effect: LibraryEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
