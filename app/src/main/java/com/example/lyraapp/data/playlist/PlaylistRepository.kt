
package com.example.lyraapp.data.playlist

import com.example.lyraapp.data.home.formatDurationMs
import com.example.lyraapp.data.remote.ApiErrorMapper
import com.example.lyraapp.data.remote.LyraApiService
import com.example.lyraapp.data.remote.dto.AddPlaylistTrackBody
import com.example.lyraapp.data.remote.dto.CreatePlaylistRequest
import com.example.lyraapp.data.remote.dto.SongDto
import com.example.lyraapp.ui.home.PlayableItem
import com.example.lyraapp.ui.library.LibraryPlaylistItem
import javax.inject.Inject
import javax.inject.Singleton

data class PlaylistDetailContent(
    val id: String,
    val title: String,
    val description: String,
    val info: String,
    val ownerId: String?,
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

data class UserPlaylistOption(
    val id: String,
    val name: String,
)

@Singleton
class PlaylistRepository @Inject constructor(
    private val api: LyraApiService,
) {

    suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetailContent> = try {
        val playlist = api.getPlaylist(playlistId).data
        val tracks = playlist.songs.mapIndexed { index, song ->
            song.toDetailTrack(index)
        }
        val totalMinutes = playlist.songs.sumOf { it.durationMs } / 60_000
        Result.success(
            PlaylistDetailContent(
                id = playlist.id,
                title = playlist.name,
                description = playlist.description.orEmpty(),
                info = "${tracks.size} şarkı • $totalMinutes dk",
                ownerId = playlist.ownerId,
                tracks = tracks,
            ),
        )
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun createNewPlaylist(name: String, description: String): Result<String> = try {
        val request = CreatePlaylistRequest(name, description)
        val playlist = api.createPlaylist(request).data
        Result.success(playlist.id)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun createNewPlaylistWithTracks(
        name: String,
        description: String,
        songIds: List<String>,
    ): Result<String> = try {
        val playlistId = createNewPlaylist(name, description).getOrThrow()
        songIds.forEach { songId ->
            api.addTrackToPlaylist(playlistId, AddPlaylistTrackBody(songId))
        }
        Result.success(playlistId)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun loadSelectableSongs(limit: Int = 50): Result<List<PlaylistDetailTrack>> = try {
        val songs = buildList {
            addAll(api.searchSongs(limit = limit).data)
            if (isEmpty()) {
                addAll(api.getRecommendations(limit = limit).data)
            }
            if (isEmpty()) {
                addAll(api.getRecentlyPlayed(limit = limit).data)
            }
        }.distinctBy { it.id }

        Result.success(songs.mapIndexed { index, song -> song.toDetailTrack(index) })
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun loadUserPlaylists(): Result<List<UserPlaylistOption>> = try {
        val playlists = api.getMyPlaylists().data.map {
            UserPlaylistOption(id = it.id, name = it.name)
        }
        Result.success(playlists)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun addTrackToPlaylist(playlistId: String, songId: String): Result<Unit> = try {
        api.addTrackToPlaylist(playlistId, AddPlaylistTrackBody(songId))
        Result.success(Unit)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, songId: String): Result<Unit> = try {
        api.removeTrackFromPlaylist(playlistId, songId)
        Result.success(Unit)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun loadPublicPlaylists(): Result<List<LibraryPlaylistItem>> = try {
        val playlists = api.getPublicPlaylists().data
        val items = playlists.mapIndexed { index, playlist ->
            val songCount = runCatching { api.getPlaylist(playlist.id).data.songs.size }.getOrDefault(0)
            LibraryPlaylistItem(
                id = playlist.id,
                title = playlist.name,
                songCount = songCount,
                gradientStartColor = coverColors[index % coverColors.size],
                gradientEndColor = coverColors[(index + 1) % coverColors.size],
            )
        }
        Result.success(items)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun loadRecentlyPlayed(limit: Int = 50): Result<List<PlayableItem>> = try {
        val songs = api.getRecentlyPlayed(limit = limit).data
        Result.success(
            songs.mapIndexed { index, song ->
                PlayableItem(
                    id = song.id,
                    title = song.title,
                    subtitle = song.artist,
                    gradientIndex = index,
                    durationMs = song.durationMs,
                )
            },
        )
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    suspend fun getSongDetail(songId: String): Result<SongDto> = try {
        Result.success(api.getSong(songId).data)
    } catch (exception: Exception) {
        Result.failure(IllegalArgumentException(ApiErrorMapper.toMessage(exception)))
    }

    private fun SongDto.toDetailTrack(index: Int): PlaylistDetailTrack = PlaylistDetailTrack(
        id = id,
        title = title,
        artist = artist,
        duration = formatDurationMs(durationMs),
        durationMs = durationMs,
        coverColor = coverColors[index % coverColors.size],
    )

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
