package com.example.lyraapp.ui.navigation

sealed class LyraDestination(val route: String) {
    data object Login : LyraDestination("login")
    data object Register : LyraDestination("register")
    data object Home : LyraDestination("home")
    data object Search : LyraDestination("search")
    data object Library : LyraDestination("library")
    data object Favorites : LyraDestination("favorites")
    data object Profile : LyraDestination("profile")
    data object PlaylistDetail : LyraDestination("playlist_detail")
    data object CreatePlaylist : LyraDestination("create_playlist")
}