package com.example.lyraapp.data.playlist

import com.example.lyraapp.data.home.formatDurationMs
import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import javax.inject.Inject
import javax.inject.Singleton

data class PlaylistDetailContent(
    val id: String,
    val title: String,
    val description: String,
    val info: String,
    val tracks: List<PlaylistDetailTrack>,
)

data class PlaylistDetailTrack(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val durationMs: Long,
    val coverColor: Long,
)

@Singleton
class PlaylistRepository @Inject constructor(
    private val api: LyraApiService,
) {

    suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetailContent> = try {
        val playlist = api.getPlaylist(playlistId).data
        val tracks = playlist.songs.mapIndexed { index, song ->
            PlaylistDetailTrack(
                id = song.id,
                title = song.title,
                artist = song.artist,
                duration = formatDurationMs(song.durationMs),
                durationMs = song.durationMs,
                coverColor = coverColors[index % coverColors.size],
            )
        }
        val totalMinutes = playlist.songs.sumOf { it.durationMs } / 60_000
        Result.success(
            PlaylistDetailContent(
                id = playlist.id,
                title = playlist.name,
                description = playlist.description.orEmpty(),
                info = "${tracks.size} şarkı • $totalMinutes dk",
                tracks = tracks,
            ),
        )
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    private companion object {
        val coverColors = listOf(
            0xFF8A5A44,
            0xFF65A765,
            0xFF4A90E2,
            0xFF48C9B0,
            0xFF45B39D,
        )
    }
}
