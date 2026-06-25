package com.example.lyraapp.data.home

import com.example.lyraapp.data.remote.dto.SongDto
import com.example.lyraapp.ui.home.PlayableItem

object SongMapper {

    fun toPlayableItems(songs: List<SongDto>): List<PlayableItem> =
        songs.mapIndexed { index, song -> song.toPlayableItem(index) }

    private fun SongDto.toPlayableItem(index: Int): PlayableItem = PlayableItem(
        id = id,
        title = title,
        subtitle = artist,
        gradientIndex = index,
        durationMs = durationMs,
    )
}

internal fun formatDurationMs(durationMs: Long): String {
    if (durationMs <= 0L) return "0:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
