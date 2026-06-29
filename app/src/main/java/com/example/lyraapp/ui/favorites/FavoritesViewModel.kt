package com.example.lyraapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.favorites.FavoritesRepository
import com.example.lyraapp.data.favorites.FavoritesRepository.Companion.toUiModel
import com.example.lyraapp.data.favorites.StoredFavorite
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
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
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesContract.State())
    val state: StateFlow<FavoritesContract.State> = _state.asStateFlow()

    private val _effect = Channel<FavoritesContract.SideEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var storedTracks: List<StoredFavorite> = emptyList()

    init {
        viewModelScope.launch {
            favoritesRepository.favorites.collect { favorites ->
                storedTracks = favorites
                refreshState()
            }
        }
    }

    fun onIntent(intent: FavoritesContract.Intent) {
        when (intent) {
            is FavoritesContract.Intent.OnSongClick -> playTrack(intent.songId, shuffle = false)
            is FavoritesContract.Intent.OnSongLongClick -> viewModelScope.launch {
                _effect.send(FavoritesContract.SideEffect.NavigateToSongDetail(intent.songId))
            }
            is FavoritesContract.Intent.OnRemoveFromFavorites -> viewModelScope.launch {
                favoritesRepository.remove(intent.songId)
                _effect.send(FavoritesContract.SideEffect.ShowToast("Favorilerden kaldırıldı"))
            }
            FavoritesContract.Intent.OnPlayAllClick -> playTrack(storedTracks.firstOrNull()?.id.orEmpty(), shuffle = false)
            FavoritesContract.Intent.OnShuffleClick -> playTrack(storedTracks.firstOrNull()?.id.orEmpty(), shuffle = true)
        }
    }

    private fun playTrack(trackId: String, shuffle: Boolean) {
        if (storedTracks.isEmpty() || trackId.isBlank()) return
        viewModelScope.launch {
            val queue = if (shuffle) storedTracks.shuffled() else storedTracks
            val tracks = queue.map { it.toPlaybackTrack() }
            val selected = tracks.find { it.id == trackId } ?: tracks.first()
            val index = tracks.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
            playerRepository.playTrack(track = tracks[index], queue = tracks, startIndex = index)
            _effect.send(FavoritesContract.SideEffect.NavigateToPlayer)
        }
    }

    private fun refreshState() {
        val totalMs = storedTracks.sumOf { it.durationMs }
        val totalSeconds = totalMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        _state.update {
            it.copy(
                songs = storedTracks.map { favorite -> favorite.toUiModel() },
                totalSongsCount = storedTracks.size,
                totalDurationText = "${storedTracks.size} şarkı • ${minutes} dk ${seconds} sn",
            )
        }
    }
}
