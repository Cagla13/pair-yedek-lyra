package com.example.lyraapp.ui.library

import androidx.compose.runtime.Immutable

@Immutable
data class LibraryUiState(
    val selectedFilter: LibraryFilter = LibraryFilter.Playlists,
    val sortLabel: String = "Son eklenenler",
    val isGridView: Boolean = false,
    val playlists: List<LibraryPlaylistItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@Immutable
data class LibraryPlaylistItem(
    val id: String,
    val title: String,
    val songCount: Int,
    val gradientStartColor: Long,
    val gradientEndColor: Long,
    val isPinned: Boolean = false,
    val showsHeartIcon: Boolean = false,
)

enum class LibraryFilter {
    Playlists,
    Artists,
    Albums,
}

sealed interface LibraryIntent {
    data class FilterSelected(val filter: LibraryFilter) : LibraryIntent
    data object ToggleViewMode : LibraryIntent
    data object SearchClicked : LibraryIntent
    data object CreatePlaylistClicked : LibraryIntent
    data class PlaylistClicked(val playlistId: String) : LibraryIntent
    data class PlaylistMenuClicked(val playlistId: String) : LibraryIntent
    data object RetryLoad : LibraryIntent
}

sealed interface LibraryEffect {
    data object NavigateToSearch : LibraryEffect
    data object NavigateToCreatePlaylist : LibraryEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect
    data class ShowMessage(val message: String) : LibraryEffect
}
