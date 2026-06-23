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
    data object Player : LyraDestination("player")
    data object NotificationPreview : LyraDestination("notification_preview")
}