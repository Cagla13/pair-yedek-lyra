package com.example.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.catalog.CatalogAlbum
import com.example.lyraapp.data.catalog.CatalogArtist
import com.example.lyraapp.data.catalog.CatalogRepository
import com.example.lyraapp.data.favorites.FavoritesRepository
import com.example.lyraapp.data.home.formatDurationMs
import com.example.lyraapp.data.library.LibraryRepository
import com.example.lyraapp.data.player.PlayerRepository
import com.example.lyraapp.data.player.toPlaybackTrack
import com.example.lyraapp.data.playlist.PlaylistRepository
import com.example.lyraapp.data.remote.dto.SongDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val catalogRepository: CatalogRepository,
    private val favoritesRepository: FavoritesRepository,
    private val playerRepository: PlayerRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState(isLoading = true))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var catalogSongs: List<SongDto> = emptyList()

    companion object {
        const val LIKED_SONGS_ID = "liked_songs_id"
    }

    init {
        loadPlaylists()
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = intent.filter, catalogTitle = null) }
                when (intent.filter) {
                    LibraryFilter.Artists -> loadArtists()
                    LibraryFilter.Albums -> loadAlbums()
                    LibraryFilter.Playlists -> loadPlaylists()
                }
            }
            LibraryIntent.ToggleViewMode -> {
                _uiState.update { it.copy(isGridView = !it.isGridView) }
            }
            LibraryIntent.SearchClicked -> sendEffect(LibraryEffect.NavigateToSearch)
            LibraryIntent.CreatePlaylistClicked -> sendEffect(LibraryEffect.NavigateToCreatePlaylist)
            is LibraryIntent.PlaylistClicked -> {
                if (intent.playlistId == LIKED_SONGS_ID) {
                    sendEffect(LibraryEffect.NavigateToFavorites)
                } else {
                    sendEffect(LibraryEffect.NavigateToPlaylistDetail(intent.playlistId))
                }
            }
            is LibraryIntent.RequestDeletePlaylist -> showDeletePrompt(intent.playlistId)
            LibraryIntent.DismissDeletePlaylist -> {
                _uiState.update { it.copy(deletePrompt = null) }
            }
            LibraryIntent.ConfirmDeletePlaylist -> {
                val prompt = _uiState.value.deletePrompt ?: return
                _uiState.update { it.copy(deletePrompt = null) }
                deletePlaylist(prompt.playlistId)
            }
            LibraryIntent.RetryLoad -> reloadCurrentFilter()
            is LibraryIntent.ArtistClicked -> openCatalog(title = intent.name, songsLoader = {
                catalogRepository.songsByArtist(intent.name)
            })
            is LibraryIntent.AlbumClicked -> openCatalog(
                title = "${intent.title} • ${intent.artist}",
                songsLoader = { catalogRepository.songsByAlbum(intent.title, intent.artist) },
            )
            is LibraryIntent.CatalogSongClicked -> playCatalogSong(intent.songId)
            is LibraryIntent.CatalogSongLongClicked -> sendEffect(LibraryEffect.NavigateToSongDetail(intent.songId))
            LibraryIntent.CatalogBackClicked -> {
                _uiState.update { it.copy(catalogTitle = null, catalogSongs = emptyList()) }
            }
        }
    }

    private fun reloadCurrentFilter() {
        when (_uiState.value.selectedFilter) {
            LibraryFilter.Playlists -> loadPlaylists()
            LibraryFilter.Artists -> loadArtists()
            LibraryFilter.Albums -> loadAlbums()
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val favoriteCount = favoritesRepository.getFavorites().size
            libraryRepository.loadPlaylists()
                .onSuccess { apiPlaylists ->
                    val likedSongsItem = LibraryPlaylistItem(
                        id = LIKED_SONGS_ID,
                        title = "Beğenilen Şarkılar",
                        songCount = favoriteCount,
                        gradientStartColor = 0xFFFFB1C8,
                        gradientEndColor = 0xFFEFBD94,
                        isPinned = true,
                        showsHeartIcon = true,
                    )
                    _uiState.update {
                        it.copy(isLoading = false, playlists = listOf(likedSongsItem) + apiPlaylists)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Kütüphane yüklenemedi.")
                    }
                    sendEffect(LibraryEffect.ShowMessage(error.message ?: "Kütüphane yüklenemedi."))
                }
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            catalogRepository.loadArtists()
                .onSuccess { artists ->
                    _uiState.update { it.copy(isLoading = false, artists = artists) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Sanatçılar yüklenemedi.")
                    }
                }
        }
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            catalogRepository.loadAlbums()
                .onSuccess { albums ->
                    _uiState.update { it.copy(isLoading = false, albums = albums) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Albümler yüklenemedi.")
                    }
                }
        }
    }

    private fun openCatalog(
        title: String,
        songsLoader: suspend () -> Result<List<SongDto>>,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            songsLoader()
                .onSuccess { songs ->
                    catalogSongs = songs
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            catalogTitle = title,
                            catalogSongs = songs.map { song ->
                                CatalogSongItem(
                                    id = song.id,
                                    title = song.title,
                                    artist = song.artist,
                                    duration = formatDurationMs(song.durationMs),
                                )
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    sendEffect(LibraryEffect.ShowMessage(error.message ?: "Şarkılar yüklenemedi."))
                }
        }
    }

    private fun playCatalogSong(songId: String) {
        val songs = catalogSongs
        if (songs.isEmpty()) return
        viewModelScope.launch {
            val queue = songs.map { it.toPlaybackTrack(_uiState.value.catalogTitle.orEmpty()) }
            val index = queue.indexOfFirst { it.id == songId }.coerceAtLeast(0)
            playerRepository.playTrack(track = queue[index], queue = queue, startIndex = index)
            sendEffect(LibraryEffect.NavigateToPlayer)
        }
    }

    private fun showDeletePrompt(playlistId: String) {
        if (playlistId == LIKED_SONGS_ID) {
            sendEffect(LibraryEffect.ShowMessage("Beğenilen şarkılar silinemez."))
            return
        }
        val playlist = _uiState.value.playlists.find { it.id == playlistId } ?: return
        _uiState.update {
            it.copy(deletePrompt = LibraryDeletePrompt(playlistId = playlist.id, title = playlist.title))
        }
    }

    private fun deletePlaylist(playlistId: String) {
        if (playlistId == LIKED_SONGS_ID) {
            sendEffect(LibraryEffect.ShowMessage("Beğenilen şarkılar silinemez."))
            return
        }
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
                .onSuccess {
                    sendEffect(LibraryEffect.ShowMessage("Çalma listesi silindi."))
                    loadPlaylists()
                }
                .onFailure { error ->
                    sendEffect(LibraryEffect.ShowMessage(error.message ?: "Çalma listesi silinemedi."))
                }
        }
    }

    private fun sendEffect(effect: LibraryEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
