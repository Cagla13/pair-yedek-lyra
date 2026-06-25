package com.example.lyraapp.data.library

import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.ui.library.LibraryPlaylistItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val api: LyraApiService,
) {

    suspend fun loadPlaylists(): Result<List<LibraryPlaylistItem>> = try {
        val playlists = api.getMyPlaylists().data
        val items = playlists.mapIndexed { index, playlist ->
            val songCount = runCatching {
                api.getPlaylist(playlist.id).data.songs.size
            }.getOrDefault(0)
            playlist.toLibraryItem(index = index, songCount = songCount)
        }
        Result.success(items)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    private fun com.example.lyraapp.data.remote.dto.PlaylistDto.toLibraryItem(
        index: Int,
        songCount: Int,
    ): LibraryPlaylistItem {
        val gradient = LibraryGradients.forIndex(index)
        return LibraryPlaylistItem(
            id = id,
            title = name,
            songCount = songCount,
            gradientStartColor = gradient.first,
            gradientEndColor = gradient.second,
        )
    }
}

private object LibraryGradients {
    private val palette = listOf(
        0xFF8E2DE2 to 0xFF4A00E0,
        0xFF5C6BC0 to 0xFF3949AB,
        0xFF26A69A to 0xFF00897B,
        0xFF4DD0E1 to 0xFF0097A7,
        0xFF66BB6A to 0xFF2E7D32,
        0xFFFFB1C8 to 0xFFEFBD94,
    )

    fun forIndex(index: Int): Pair<Long, Long> = palette[index % palette.size]
}
