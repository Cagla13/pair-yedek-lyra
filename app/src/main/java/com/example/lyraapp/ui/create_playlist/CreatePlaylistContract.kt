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
        val isLoadingTracks: Boolean = true,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false,
        val errorMessage: String? = null,
        val availableTracks: List<SelectableTrack> = emptyList(),
    ) {
        val selectedCount: Int
            get() = availableTracks.count { it.isSelected }

        val selectedTrackIds: List<String>
            get() = availableTracks.filter { it.isSelected }.map { it.id }
    }

    sealed class Event {
        data class OnNameChanged(val name: String) : Event()
        data class OnDescriptionChanged(val description: String) : Event()
        data class OnPublicToggleChanged(val isPublic: Boolean) : Event()
        data class OnTrackSelectionToggled(val trackId: String) : Event()
        data object OnSaveClicked : Event()
        data object OnCloseClicked : Event()
        data object RetryLoadTracks : Event()
    }
}