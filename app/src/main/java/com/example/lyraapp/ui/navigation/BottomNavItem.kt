package com.example.lyraapp.ui.navigation

import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lyraapp.ui.icons.LyraIcons

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(LyraDestination.Home.route, "Ana sayfa", LyraIcons.Home)
    data object Search : BottomNavItem(LyraDestination.Search.route, "Ara", LyraIcons.Search)
    data object Library : BottomNavItem(LyraDestination.Library.route, "Kütüphane", LyraIcons.LibraryMusic)
    data object Favorites : BottomNavItem(LyraDestination.Favorites.route, "Favoriler", androidx.compose.material.icons.Icons.Default.List)
    data object Profile : BottomNavItem(LyraDestination.Profile.route, "Profil", androidx.compose.material.icons.Icons.Default.List)

    companion object {
        val items = listOf(Home, Search, Library, Favorites, Profile)
    }
}