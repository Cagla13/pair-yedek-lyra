package com.example.lyraapp.ui.profile

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileUiState(
    val displayName: String = "",
    val handle: String = "",
    val isPremium: Boolean = false,
    val avatarInitials: String = "",
    val stats: List<ProfileStat> = emptyList(),
    val settings: List<ProfileSettingItem> = emptyList(),
    val isLoading: Boolean = false,
)

@Immutable
data class ProfileStat(
    val value: String,
    val label: String,
)

@Immutable
data class ProfileSettingItem(
    val id: String,
    val title: String,
    val value: String? = null,
    val iconKey: ProfileSettingIcon = ProfileSettingIcon.Notifications,
)

enum class ProfileSettingIcon {
    SoundQuality,
    OfflineDownload,
    Notifications,
    Privacy,
    Help,
}

sealed interface ProfileIntent {
    data object SettingsClicked : ProfileIntent
    data object EditProfileClicked : ProfileIntent
    data class SettingClicked(val id: String) : ProfileIntent
    data object LogoutClicked : ProfileIntent
}

sealed interface ProfileEffect {
    data class ShowMessage(val message: String) : ProfileEffect
    data object NavigateToLogin : ProfileEffect
    data object NavigateToEditProfile : ProfileEffect
}
