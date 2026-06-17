package com.example.lyraapp.ui.playlist_detail

class PlaylistDetailContract {

    data class Track(
        val id: Int,
        val title: String,
        val artist: String,
        val duration: String,
        val isLiked: Boolean,
        val isPlaying: Boolean,
        val coverColor: Long
    )

    data class State(
        val playlistTitle: String = "Gece Sürüşü",
        val playlistDescription: String = "Karanlık yollar için synth-pop",
        val playlistInfo: String = "Zeynep Kaya • 6 şarkı • 23 dk",
        val tracks: List<Track> = listOf(
            Track(1, "Neon Sokaklar", "Şehir Işıkları", "3:43", true, true, 0xFF8A5A44),
            Track(2, "Gece Yarısı", "Mavi Deniz", "3:34", true, false, 0xFF65A765),
            Track(3, "Mor Bulutlar", "Derin Kaya", "3:52", false, false, 0xFF4A90E2),
            Track(4, "Son Tren", "Peron", "3:37", false, false, 0xFF48C9B0),
            Track(5, "Yıldız Tozu", "Polaris", "4:07", true, false, 0xFF45B39D)
        )
    )

    sealed class Event {
        data object OnPlayClicked : Event()
        data object OnBackClicked : Event()
        data class OnTrackClicked(val trackId: Int) : Event()
        data class OnLikeClicked(val trackId: Int) : Event()
    }
}