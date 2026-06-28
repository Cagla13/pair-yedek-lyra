package com.example.lyraapp.ui.icons

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * LyraApp ikon seti.
 */
object LyraIcons {

    val Home = androidx.compose.material.icons.Icons.Default.Home
    val Search = androidx.compose.material.icons.Icons.Default.Search
    val LibraryMusic = androidx.compose.material.icons.Icons.Default.LibraryMusic

    val PlayArrow: ImageVector by lazy {
        lyraIcon("PlayArrow", "M8,5v14l11,-7z")
    }

    val Pause: ImageVector by lazy {
        lyraIcon("Pause", "M6,19h4V5H6v14zM14,5v14h4V5h-4z")
    }

    val FavoriteBorder: ImageVector by lazy {
        lyraIcon("FavoriteBorder", "M16.5,3c-1.74,0 -3.41,0.81 -4.5,2.09C10.91,3.81 9.24,3 7.5,3C4.42,3 2,5.42 2,8.5c0,3.78 3.4,6.86 8.55,11.54L12,21.35l1.45,-1.32C18.6,15.36 22,12.28 22,8.5C22,5.42 19.58,3 16.5,3zM12.1,18.55l-0.1,0.1l-0.1,-0.1C7.14,14.24 4,11.39 4,8.5c0,-1.74 1.26,-3 3,-3c1.52,0 2.98,0.98 3.5,2.34h1.99c0.52,-1.36 1.98,-2.34 3.5,-2.34c1.74,0 3,1.26 3,3c0,2.89 -3.14,5.74 -8.1,10.05z")
    }

    val Favorite: ImageVector by lazy {
        lyraIcon("Favorite", "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5C2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z")
    }

    val Waveform: ImageVector by lazy {
        lyraIcon("Waveform", "M7,18h2V6H7v12zM11,22h2V2h-2v20zM3,14h2v-4H3v4zM15,18h2V6h-2v12zM19,10v4h2v-4h-2z")
    }

    val Smartphone: ImageVector by lazy {
        lyraIcon("Smartphone", "M15.5,1h-8C6.12,1 5,2.12 5,3.5v17C5,21.88 6.12,23 7.5,23h8c1.38,0 2.5,-1.12 2.5,-2.5v-17C18,2.12 16.88,1 15.5,1zM13,21h-3v-1h3v1zM16.25,18H6.75V4h9.5V18z")
    }

    val Lock: ImageVector by lazy {
        lyraIcon("Lock", "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM12,17c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2zM15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 1.71,0 3.1,1.39 3.1,3.1v2z")
    }

    val Visibility: ImageVector by lazy {
        lyraIcon("Visibility", "M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73,-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z")
    }

    val ArrowForward: ImageVector by lazy {
        lyraIcon("ArrowForward", "M12,4l-1.41,1.41L16.17,11H4v2h12.17l-5.58,5.59L12,20l8,-8z")
    }

    val ArrowBack: ImageVector by lazy {
        lyraIcon("ArrowBack", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z")
    }

    val Shield: ImageVector by lazy {
        lyraIcon("Shield", "M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12V5l-9,-4zM12,11.99h7c-0.53,4.12 -3.28,7.79 -7,8.94V12H5V6.3l7,-3.11v8.8z")
    }

    val CreditCard: ImageVector by lazy {
        lyraIcon("CreditCard", "M20,4H4C2.89,4 2.01,4.89 2.01,6L2,18c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V6C22,4.89 21.11,4 20,4zM20,18H4v-6h16V18zM20,8H4V6h16V8z")
    }

    val WorkspacePremium: ImageVector by lazy {
        lyraIcon("WorkspacePremium", "M9.68,13.69L12,11.93l2.31,1.76l-0.88,-2.85L15.75,9h-2.84L12,6.19L11.09,9H8.25l2.31,1.84L9.68,13.69zM20,10c0,-4.42 -3.58,-8 -8,-8s-8,3.58 -8,8c0,2.03 0.76,3.87 2,5.28V23l6,-2l6,2v-7.72C19.24,13.87 20,12.03 20,10zM12,4c3.31,0 6,2.69 6,6s-2.69,6 -6,6s-6,-2.69 -6,-6S8.69,4 12,4z")
    }
}

private fun lyraIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()
