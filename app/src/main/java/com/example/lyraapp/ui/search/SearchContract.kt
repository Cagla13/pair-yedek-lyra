package com.example.lyraapp.ui.search

import androidx.compose.runtime.Immutable

/**
 * Search ekranının MVI Kontratı.
 */
object SearchContract {

    @Immutable
    data class State(
        val searchQuery: String = "",
        val selectedCategory: String = "Hepsi",
        val categories: List<String> = listOf("Hepsi", "Pop", "Elektronik", "Akustik"),
        val genres: List<GenreUiModel> = listOf(
            GenreUiModel("Pop", "0xFF54C5B3"), // Yeşil tonları
            GenreUiModel("Elektronik", "0xFF9B86FA"), // Mor tonları
            GenreUiModel("Akustik", "0xFFB570A9"), // Pembe/Eflatun tonları
            GenreUiModel("Lo-fi", "0xFF3F777B"), // Mavi/Petroller
            GenreUiModel("Indie", "0xFF5E548E"), // Koyu mor
            GenreUiModel("Jazz", "0xFF537A3B"), // Yeşil/Zeytin
            GenreUiModel("Klasik", "0xFFB55D75"), // Gül kurusu
            GenreUiModel("Yolculuk", "0xFFE28766") // Turuncu/Kiremit
        ),
        val isLoading: Boolean = false
    )

    sealed interface Intent {
        data class OnSearchQueryChanged(val query: String) : Intent
        data class OnCategorySelected(val category: String) : Intent
        data class OnGenreClick(val genreName: String) : Intent
    }

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}

data class GenreUiModel(
    val name: String,
    val colorHex: String
)