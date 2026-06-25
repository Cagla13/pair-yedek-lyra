package com.example.lyraapp.ui.favorites

object FavoritesStorage {
    // Başlangıçta tamamen boş gelen dinamik favori listesi
    val savedSongsList: MutableList<SongUiModel> = mutableListOf()

    // Şarkıların sürelerini ("3:34" gibi) saniye bazında toplayıp formatlayan fonksiyon
    fun getTotalDurationText(): String {
        if (savedSongsList.isEmpty()) {
            return "0 şarkı • 0 dk 0 sn"
        }

        var totalSeconds = 0
        savedSongsList.forEach { song ->
            val parts = song.duration.split(":")
            if (parts.size == 2) {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toIntOrNull() ?: 0
                totalSeconds += (minutes * 60) + seconds
            }
        }

        val displayMinutes = totalSeconds / 60
        val displaySeconds = totalSeconds % 60

        return "${savedSongsList.size} şarkı • ${displayMinutes} dk ${displaySeconds} sn"
    }
}