package com.example.lyraapp.data.search

import com.example.lyraapp.data.home.formatDurationMs
import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import javax.inject.Inject
import javax.inject.Singleton

data class SearchSongItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationLabel: String,
    val durationMs: Long,
)

@Singleton
class SearchRepository @Inject constructor(
    private val api: LyraApiService,
) {

    suspend fun searchSongs(query: String, limit: Int = 20): Result<List<SearchSongItem>> {
        return try {
            val trimmed = query.trim()
            if (trimmed.isBlank()) {
                Result.success(emptyList())
            } else {
                val response = api.searchSongs(query = trimmed, limit = limit)
                Result.success(response.data.map { it.toSearchSongItem() })
            }
        } catch (exception: Exception) {
            Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
        }
    }

    private fun com.example.lyraapp.data.remote.dto.SongDto.toSearchSongItem(): SearchSongItem =
        SearchSongItem(
            id = id,
            title = title,
            artist = artist,
            album = album.orEmpty(),
            durationLabel = formatDurationMs(durationMs),
            durationMs = durationMs,
        )
}
