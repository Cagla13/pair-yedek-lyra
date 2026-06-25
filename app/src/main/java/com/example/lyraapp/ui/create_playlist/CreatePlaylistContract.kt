package com.example.lyraapp.ui.create_playlist

class CreatePlaylistContract {

    data class SelectableTrack(
        val id: String,
        val title: String,
        val artist: String,
        val coverColor: Long,
        val isSelected: Boolean = false
    )

    data class State(
        val playlistName: String = "",
        val playlistDescription: String = "",
        val isPublic: Boolean = true,
        val availableTracks: List<SelectableTrack> = listOf(
            SelectableTrack("1", "Gece Yarısı", "Mavi Deniz", 0xFF81C784),
            SelectableTrack("2", "Sessiz Şehir", "Ela Tuna", 0xFF9575CD),
            SelectableTrack("3", "Yıldız Tozu", "Polaris", 0xFF4DB6AC),
            SelectableTrack("4", "Sahil Yolu", "Kumsal", 0xFFD87E71),
            SelectableTrack("5", "Mor Bulutlar", "Derin Kaya", 0xFF4DD0E1),
            SelectableTrack("6", "İlk Işık", "Sabah Ezgisi", 0xFF4FC3F7),
            SelectableTrack("7", "Kayıp Anılar", "Eko", 0xFF81D4FA)
        )
    ) {
        val selectedCount: Int
            get() = availableTracks.count { it.isSelected }
    }

    sealed class Event {
        data class OnNameChanged(val name: String) : Event()
        data class OnDescriptionChanged(val description: String) : Event()
        data class OnPublicToggleChanged(val isPublic: Boolean) : Event()
        data class OnTrackSelectionToggled(val trackId: String) : Event()
        data object OnSaveClicked : Event()
        data object OnCloseClicked : Event()
    }
}