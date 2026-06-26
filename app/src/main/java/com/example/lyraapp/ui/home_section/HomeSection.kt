package com.example.lyraapp.ui.home_section

enum class HomeSection(
    val routeKey: String,
    val title: String,
) {
    FOR_YOU("for_you", "Senin için müzikler"),
    RECOMMENDATIONS("recommendations", "Sana önerilenler"),
    FEATURED_PLAYLISTS("featured_playlists", "Öne çıkan listeler"),
    ;

    companion object {
        fun fromRouteKey(key: String): HomeSection? =
            entries.find { it.routeKey == key }
    }
}
