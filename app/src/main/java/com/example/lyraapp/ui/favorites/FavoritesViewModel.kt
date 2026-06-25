package com.example.lyraapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<FavoritesContract.State>(FavoritesContract.State())
    val state: StateFlow<FavoritesContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<FavoritesContract.SideEffect>()
    val effect: SharedFlow<FavoritesContract.SideEffect> = _effect.asSharedFlow()

    init {
        refreshFavorites()
    }

    // Ortak depodaki verileri ve milimetrik saniye hesabını arayüze senkronize eder
    fun refreshFavorites() {
        _state.update {
            it.copy(
                songs = FavoritesStorage.savedSongsList.toList(),
                totalSongsCount = FavoritesStorage.savedSongsList.size,
                totalDurationText = FavoritesStorage.getTotalDurationText()
            )
        }
    }

    fun onIntent(intent: FavoritesContract.Intent) {
        when (intent) {
            is FavoritesContract.Intent.OnSongClick -> {
                FavoritesStorage.savedSongsList.indices.forEach { i ->
                    val song = FavoritesStorage.savedSongsList[i]
                    FavoritesStorage.savedSongsList[i] = song.copy(isPlaying = song.id == intent.songId)
                }
                refreshFavorites()
            }
            is FavoritesContract.Intent.OnRemoveFromFavorites -> {
                FavoritesStorage.savedSongsList.removeAll { it.id == intent.songId }
                refreshFavorites()

                viewModelScope.launch {
                    _effect.emit(FavoritesContract.SideEffect.ShowToast("Favorilerden kaldırıldı"))
                }
            }
            FavoritesContract.Intent.OnPlayAllClick -> {
                viewModelScope.launch { _effect.emit(FavoritesContract.SideEffect.ShowToast("Tüm şarkılar oynatılıyor...")) }
            }
            FavoritesContract.Intent.OnShuffleClick -> {
                viewModelScope.launch { _effect.emit(FavoritesContract.SideEffect.ShowToast("Karışık çalma açıldı")) }
            }
        }
    }
}