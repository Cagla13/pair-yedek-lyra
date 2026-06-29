package com.example.lyraapp.ui.library

import androidx.compose.runtime.Immutable
import com.example.lyraapp.data.catalog.CatalogAlbum
import com.example.lyraapp.data.catalog.CatalogArtist

@Immutable
data class LibraryDeletePrompt(
    val playlistId: String,
    val title: String,
)

@Immutable
data class LibraryUiState(
    val selectedFilter: LibraryFilter = LibraryFilter.Playlists,
    val sortLabel: String = "Son eklenenler",
    val isGridView: Boolean = false,
    val playlists: List<LibraryPlaylistItem> = emptyList(),
    val artists: List<CatalogArtist> = emptyList(),
    val albums: List<CatalogAlbum> = emptyList(),
    val catalogTitle: String? = null,
    val catalogSongs: List<CatalogSongItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val deletePrompt: LibraryDeletePrompt? = null,
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

@Immutable
data class CatalogSongItem(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
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
    data class RequestDeletePlaylist(val playlistId: String) : LibraryIntent
    data object DismissDeletePlaylist : LibraryIntent
    data object ConfirmDeletePlaylist : LibraryIntent
    data class ArtistClicked(val name: String) : LibraryIntent
    data class AlbumClicked(val title: String, val artist: String) : LibraryIntent
    data class CatalogSongClicked(val songId: String) : LibraryIntent
    data class CatalogSongLongClicked(val songId: String) : LibraryIntent
    data object CatalogBackClicked : LibraryIntent
    data object RetryLoad : LibraryIntent
}

sealed interface LibraryEffect {
    data object NavigateToSearch : LibraryEffect
    data object NavigateToCreatePlaylist : LibraryEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect
    data object NavigateToFavorites : LibraryEffect
    data object NavigateToPlayer : LibraryEffect
    data class NavigateToSongDetail(val songId: String) : LibraryEffect
    data class ShowMessage(val message: String) : LibraryEffect
}
