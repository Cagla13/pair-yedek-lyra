package com.example.lyraapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
import com.example.lyraapp.data.search.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchContract.State())
    val state: StateFlow<SearchContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SearchContract.SideEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect: SharedFlow<SearchContract.SideEffect> = _effect.asSharedFlow()

    private var searchJob: Job? = null

    fun onIntent(intent: SearchContract.Intent) {
        when (intent) {
            is SearchContract.Intent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = intent.query, errorMessage = null) }
                scheduleSearch(intent.query)
            }
            is SearchContract.Intent.OnCategorySelected -> {
                _state.update { it.copy(selectedCategory = intent.category) }
                val query = buildQuery(_state.value.searchQuery, intent.category)
                if (query.isNotBlank()) {
                    scheduleSearch(query, replaceQuery = true)
                }
            }
            is SearchContract.Intent.OnGenreClick -> {
                _state.update { it.copy(searchQuery = intent.genreName, selectedCategory = "Hepsi") }
                scheduleSearch(intent.genreName, replaceQuery = true)
            }
            is SearchContract.Intent.OnSongClick -> playSong(intent.songId)
        }
    }

    private fun buildQuery(baseQuery: String, category: String): String =
        when {
            baseQuery.isNotBlank() -> baseQuery
            category != "Hepsi" -> category
            else -> ""
        }

    private fun scheduleSearch(query: String, replaceQuery: Boolean = false) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isLoading = false) }
            return
        }
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            delay(350)
            searchRepository.searchSongs(query)
                .onSuccess { results ->
                    _state.update { current ->
                        current.copy(
                            searchQuery = if (replaceQuery) query else current.searchQuery,
                            searchResults = results,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                            searchResults = emptyList(),
                        )
                    }
                }
        }
    }

    private fun playSong(songId: String) {
        val song = _state.value.searchResults.find { it.id == songId } ?: return
        viewModelScope.launch {
            val playbackTrack = song.toPlaybackTrack()
            val queue = _state.value.searchResults.map { it.toPlaybackTrack() }
            val index = queue.indexOfFirst { it.id == songId }.coerceAtLeast(0)
            playerRepository.playTrack(
                track = playbackTrack,
                queue = queue,
                startIndex = index,
            )
            _effect.emit(SearchContract.SideEffect.NavigateToPlayer)
        }
    }
}
