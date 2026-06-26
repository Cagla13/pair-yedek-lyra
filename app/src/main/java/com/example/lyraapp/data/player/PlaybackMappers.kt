package com.example.lyraapp.data.player

import com.example.lyraapp.data.home.formatDurationMs
import com.example.lyraapp.data.remote.dto.SongDto
import com.example.lyraapp.data.search.SearchSongItem
import com.example.lyraapp.ui.home.PlayableItem

fun PlaybackTrack.durationLabel(): String = formatDurationMs(durationMs)

fun com.example.lyraapp.data.favorites.StoredFavorite.toPlaybackTrack(): PlaybackTrack = PlaybackTrack(
    id = id,
    title = title,
    artist = artist,
    album = artist,
    sourceTitle = "Favoriler",
    durationMs = durationMs,
)

fun com.example.lyraapp.data.remote.dto.SongDto.toPlaybackTrack(sourceTitle: String = album ?: "Lyra"): PlaybackTrack =
    PlaybackTrack(
        id = id,
        title = title,
        artist = artist,
        album = album.orEmpty(),
        sourceTitle = sourceTitle,
        durationMs = durationMs,
    )

fun PlayableItem.toPlaybackTrack(): PlaybackTrack = PlaybackTrack(
    id = id,
    title = title,
    artist = subtitle.orEmpty(),
    album = subtitle.orEmpty(),
    sourceTitle = subtitle ?: "Lyra",
    durationMs = durationMs,
)

fun SearchSongItem.toPlaybackTrack(): PlaybackTrack = PlaybackTrack(
    id = id,
    title = title,
    artist = artist,
    album = album,
    sourceTitle = album.ifBlank { "Lyra" },
    durationMs = durationMs,
)

fun com.example.lyraapp.data.playlist.PlaylistDetailTrack.toPlaybackTrack(
    sourceTitle: String,
): PlaybackTrack = PlaybackTrack(
    id = id,
    title = title,
    artist = artist,
    album = sourceTitle,
    sourceTitle = sourceTitle,
    durationMs = durationMs,
)
