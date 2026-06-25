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
 *
 * Material Icons bağımlılığı eklemeden, ekranların ihtiyaç duyduğu glyph'leri
 * 24x24 viewport'lu [ImageVector] olarak tanımlar. Path'in dolgu rengi önemsizdir;
 * `Icon(...)` composable'ı `tint` ile üzerine yazar. Bu yüzden tüm path'ler
 * [Color.Black] ile doldukur ve renk daima çağrı tarafında temadan okunur.
 */
object LyraIcons {

    val Home = androidx.compose.material.icons.Icons.Default.Home
    val Search = androidx.compose.material.icons.Icons.Default.Search
    val LibraryMusic = androidx.compose.material.icons.Icons.Default.LibraryMusic

    /** Oynat butonu ikonu (Material PlayArrow). */
    val PlayArrow: ImageVector by lazy {
        lyraIcon(
            name = "PlayArrow",
            pathData = "M8,5v14l11,-7z",
        )
    }

    /** Duraklat butonu ikonu (Material Pause). */
    val Pause: ImageVector by lazy {
        lyraIcon(
            name = "Pause",
            pathData = "M6,19h4V5H6v14zM14,5v14h4V5h-4z",
        )
    }

    /** Favorilere eklenmemiş (boş kalp) ikonu (Material FavoriteBorder). */
    val FavoriteBorder: ImageVector by lazy {
        lyraIcon(
            name = "FavoriteBorder",
            pathData = "M16.5,3c-1.74,0 -3.41,0.81 -4.5,2.09C10.91,3.81 9.24,3 7.5,3C4.42,3 2,5.42 2,8.5c0,3.78 3.4,6.86 8.55,11.54L12,21.35l1.45,-1.32C18.6,15.36 22,12.28 22,8.5C22,5.42 19.58,3 16.5,3zM12.1,18.55l-0.1,0.1l-0.1,-0.1C7.14,14.24 4,11.39 4,8.5c0,-1.74 1.26,-3 3,-3c1.52,0 2.98,0.98 3.5,2.34h1.99c0.52,-1.36 1.98,-2.34 3.5,-2.34c1.74,0 3,1.26 3,3c0,2.89 -3.14,5.74 -8.1,10.05z",
        )
    }

    /** Favorilere eklenmiş (dolu kalp) ikonu (Material Favorite). */
    val Favorite: ImageVector by lazy {
        lyraIcon(
            name = "Favorite",
            pathData = "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5C2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z",
        )
    }

    /** Marka logosu: ekolayzer/dalga formu çubukları (Material GraphicEq). */
    val Waveform: ImageVector by lazy {
        lyraIcon(
            name = "Waveform",
            pathData = "M7,18h2V6H7v12zM11,22h2V2h-2v20zM3,14h2v-4H3v4zM15,18h2V6h-2v12zM19,10v4h2v-4h-2z",
        )
    }

    /** Telefon numarası alanının leading ikonu (Material Smartphone, outlined). */
    val Smartphone: ImageVector by lazy {
        lyraIcon(
            name = "Smartphone",
            pathData = "M15.5,1h-8C6.12,1 5,2.12 5,3.5v17C5,21.88 6.12,23 7.5,23h8c1.38,0 " +
                    "2.5,-1.12 2.5,-2.5v-17C18,2.12 16.88,1 15.5,1zM13,21h-3v-1h3v1zM16.25,18H6.75V4h9.5V18z",
        )
    }

    /** Şifre alanının leading ikonu (Material Lock). */
    val Lock: ImageVector by lazy {
        lyraIcon(
            name = "Lock",
            pathData = "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0," +
                    "1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM12,17c-1.1,0 -2,-0.9 " +
                    "-2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2zM15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 " +
                    "1.71,0 3.1,1.39 3.1,3.1v2z",
        )
    }

    /** Şifre görünürlük (göz) ikonu (Material Visibility). */
    val Visibility: ImageVector by lazy {
        lyraIcon(
            name = "Visibility",
            pathData = "M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73," +
                    "-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 " +
                    "-5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z",
        )
    }

    /** Giriş butonu ileri oku (Material ArrowForward). */
    val ArrowForward: ImageVector by lazy {
        lyraIcon(
            name = "ArrowForward",
            pathData = "M12,4l-1.41,1.41L16.17,11H4v2h12.17l-5.58,5.59L12,20l8,-8z",
        )
    }

    /** Üst bardaki geri oku (Material ArrowBack). */
    val ArrowBack: ImageVector by lazy {
        lyraIcon(
            name = "ArrowBack",
            pathData = "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z",
        )
    }
}

/**
 * 24x24 viewport'lu, tek path'li bir [ImageVector] üretir.
 * Path verisi standart SVG/Android `pathData` string formatındadır.
 */
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