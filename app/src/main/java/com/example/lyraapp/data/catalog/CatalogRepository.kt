package com.example.lyraapp.data.catalog

import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.data.remote.dto.SongDto
import javax.inject.Inject
import javax.inject.Singleton

data class CatalogArtist(
    val name: String,
    val songCount: Int,
)

data class CatalogAlbum(
    val title: String,
    val artist: String,
    val songCount: Int,
)

@Singleton
class CatalogRepository @Inject constructor(
    private val api: LyraApiService,
) {

    suspend fun loadSongs(limit: Int = 100): Result<List<SongDto>> = try {
        val songs = buildList {
            addAll(api.searchSongs(limit = limit).data)
            if (isEmpty()) {
                addAll(api.getForYou(limit = limit).data)
            }
        }.distinctBy { it.id }
        Result.success(songs)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun loadArtists(): Result<List<CatalogArtist>> =
        loadSongs().map { songs ->
            songs.groupBy { it.artist }
                .map { (artist, items) -> CatalogArtist(name = artist, songCount = items.size) }
                .sortedBy { it.name.lowercase() }
        }

    suspend fun loadAlbums(): Result<List<CatalogAlbum>> =
        loadSongs().map { songs ->
            songs.filter { !it.album.isNullOrBlank() }
                .groupBy { "${it.album}|${it.artist}" }
                .map { (_, items) ->
                    val first = items.first()
                    CatalogAlbum(
                        title = first.album.orEmpty(),
                        artist = first.artist,
                        songCount = items.size,
                    )
                }
                .sortedBy { it.title.lowercase() }
        }

    suspend fun songsByArtist(artist: String): Result<List<SongDto>> =
        loadSongs().map { songs -> songs.filter { it.artist == artist } }

    suspend fun songsByAlbum(album: String, artist: String): Result<List<SongDto>> =
        loadSongs().map { songs ->
            songs.filter { it.album == album && it.artist == artist }
        }
}
