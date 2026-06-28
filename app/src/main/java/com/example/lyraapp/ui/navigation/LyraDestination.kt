package com.example.lyraapp.ui.navigation

import android.net.Uri

sealed class LyraDestination(val route: String) {
    data object Login : LyraDestination("login")
    data object Register : LyraDestination("register")
    data object OtpVerify : LyraDestination("otp_verify/{phone}") {
        fun createRoute(phone: String): String = "otp_verify/${Uri.encode(phone)}"

        const val PHONE_ARG = "phone"
    }
    data object Home : LyraDestination("home")
    data object Search : LyraDestination("search")
    data object Library : LyraDestination("library")
    data object Favorites : LyraDestination("favorites")
    data object Profile : LyraDestination("profile")
    data object PlaylistDetail : LyraDestination("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: String): String = "playlist_detail/${Uri.encode(playlistId)}"

        const val PLAYLIST_ID_ARG = "playlistId"
    }
    data object CreatePlaylist : LyraDestination("create_playlist")
    data object RecentlyPlayed : LyraDestination("recently_played")
    data object HomeSection : LyraDestination("home_section/{section}") {
        fun createRoute(section: String): String = "home_section/$section"

        const val SECTION_ARG = "section"
    }
    data object EditProfile : LyraDestination("edit_profile")
    data object SongDetail : LyraDestination("song_detail/{songId}") {
        fun createRoute(songId: String): String = "song_detail/${Uri.encode(songId)}"

        const val SONG_ID_ARG = "songId"
    }
    data object Player : LyraDestination("player")
    data object NotificationPreview : LyraDestination("notification_preview")
    data object Premium : LyraDestination("premium/{plan}") {
        fun createRoute(plan: String? = null): String =
            "premium/${plan?.ifBlank { DEFAULT_PLAN } ?: DEFAULT_PLAN}"

        const val PLAN_ARG = "plan"
        const val DEFAULT_PLAN = "recurring"
    }
    data object Payment : LyraDestination("payment?price={price}&title={title}&desc={desc}") {
        fun createRoute(price: String, title: String, desc: String): String {
            return "payment?price=${Uri.encode(price)}&title=${Uri.encode(title)}&desc=${Uri.encode(desc)}"
        }
        const val PRICE_ARG = "price"
        const val TITLE_ARG = "title"
        const val DESC_ARG = "desc"
    }
}
