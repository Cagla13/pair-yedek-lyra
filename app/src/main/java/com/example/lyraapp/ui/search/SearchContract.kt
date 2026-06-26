package com.example.lyraapp.ui.search

import androidx.compose.runtime.Immutable
import com.example.lyraapp.data.search.SearchSongItem

object SearchContract {

    @Immutable
    data class State(
        val searchQuery: String = "",
        val selectedCategory: String = "Hepsi",
        val categories: List<String> = listOf("Hepsi", "Pop", "Elektronik", "Akustik"),
        val genres: List<GenreUiModel> = listOf(
            GenreUiModel("Pop", "0xFF54C5B3"),
            GenreUiModel("Elektronik", "0xFF9B86FA"),
            GenreUiModel("Akustik", "0xFFB570A9"),
            GenreUiModel("Lo-fi", "0xFF3F777B"),
            GenreUiModel("Indie", "0xFF5E548E"),
            GenreUiModel("Jazz", "0xFF537A3B"),
            GenreUiModel("Klasik", "0xFFB55D75"),
            GenreUiModel("Yolculuk", "0xFFE28766"),
        ),
        val searchResults: List<SearchSongItem> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMoreResults: Boolean = false,
        val errorMessage: String? = null,
    ) {
        val showResults: Boolean get() = searchQuery.isNotBlank()
    }

    sealed interface Intent {
        data class OnSearchQueryChanged(val query: String) : Intent
        data class OnCategorySelected(val category: String) : Intent
        data class OnGenreClick(val genreName: String) : Intent
        data class OnSongClick(val songId: String) : Intent
        data object LoadMoreResults : Intent
    }

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        data object NavigateToPlayer : SideEffect
    }
}

data class GenreUiModel(
    val name: String,
    val colorHex: String,
)
