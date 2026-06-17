package com.example.lyraapp.ui.search

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
class SearchViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<SearchContract.State>(SearchContract.State())
    val state: StateFlow<SearchContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SearchContract.SideEffect>()
    val effect: SharedFlow<SearchContract.SideEffect> = _effect.asSharedFlow()

    fun onIntent(intent: SearchContract.Intent) {
        when (intent) {
            is SearchContract.Intent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is SearchContract.Intent.OnCategorySelected -> {
                _state.update { it.copy(selectedCategory = intent.category) }
            }
            is SearchContract.Intent.OnGenreClick -> {
                viewModelScope.launch {
                    _effect.emit(SearchContract.SideEffect.ShowToast("${intent.genreName} tıklandı!"))
                }
            }
        }
    }
}