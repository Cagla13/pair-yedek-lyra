package com.example.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class LibraryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadMockPlaylists()
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
        }
    }

    private fun loadMockPlaylists() {
        _uiState.value = LibraryUiState(
            playlists = listOf(
                LibraryPlaylistItem(
                    id = "liked",
                    title = "Beğenilen Şarkılar",
                    songCount = 5,
                    gradientStartColor = 0xFFFFB1C8,
                    gradientEndColor = 0xFFEFBD94,
                    isPinned = true,
                    showsHeartIcon = true,
                ),
                LibraryPlaylistItem(
                    id = "1",
                    title = "Gece Sürüşü",
                    songCount = 5,
                    gradientStartColor = 0xFF8E2DE2,
                    gradientEndColor = 0xFF4A00E0,
                ),
                LibraryPlaylistItem(
                    id = "2",
                    title = "Sabah Kahvesi",
                    songCount = 5,
                    gradientStartColor = 0xFF5C6BC0,
                    gradientEndColor = 0xFF3949AB,
                ),
                LibraryPlaylistItem(
                    id = "3",
                    title = "Odaklan",
                    songCount = 5,
                    gradientStartColor = 0xFF26A69A,
                    gradientEndColor = 0xFF00897B,
                ),
                LibraryPlaylistItem(
                    id = "4",
                    title = "Yaz Anıları",
                    songCount = 5,
                    gradientStartColor = 0xFF4DD0E1,
                    gradientEndColor = 0xFF0097A7,
                ),
                LibraryPlaylistItem(
                    id = "5",
                    title = "Akustik Akşam",
                    songCount = 5,
                    gradientStartColor = 0xFF66BB6A,
                    gradientEndColor = 0xFF2E7D32,
                ),
            ),
        )
    }

    private fun sendEffect(effect: LibraryEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
