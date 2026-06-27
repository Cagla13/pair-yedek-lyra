package com.example.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.data.library.LibraryRepository
import com.example.lyraapp.data.settings.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val libraryRepository: LibraryRepository,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeCurrentUser()
        observeSettings()
        refreshProfile()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.SettingsClicked -> viewModelScope.launch {
                _effect.send(ProfileEffect.ShowMessage("Tema ayarları aşağıda."))
            }
            ProfileIntent.EditProfileClicked -> viewModelScope.launch {
                _effect.send(ProfileEffect.NavigateToEditProfile)
            }
            is ProfileIntent.SettingClicked -> handleSettingClick(intent.id)
            ProfileIntent.LogoutClicked -> logout()
        }
    }

    private fun handleSettingClick(id: String) {
        viewModelScope.launch {
            when (id) {
                "logout" -> logout()
                "sound_quality" -> {
                    appSettingsRepository.cycleSoundQuality()
                    _effect.send(ProfileEffect.ShowMessage("Ses kalitesi güncellendi."))
                }
                "offline_download" -> {
                    appSettingsRepository.toggleOfflineDownload()
                    _effect.send(ProfileEffect.ShowMessage("Çevrimdışı indirme ayarı güncellendi."))
                }
                "notifications" -> {
                    appSettingsRepository.toggleNotifications()
                    _effect.send(ProfileEffect.ShowMessage("Bildirim ayarı güncellendi."))
                }
                "privacy", "help" -> {
                    _effect.send(ProfileEffect.ShowMessage("Bu özellik yakında eklenecek."))
                }
            }
        }
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            authRepository.fetchCurrentUser()
            val playlistCount = libraryRepository.loadPlaylists().getOrNull()?.size ?: 0
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    stats = listOf(
                        ProfileStat(value = playlistCount.toString(), label = "Çalma listesi"),
                    ),
                )
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _effect.send(ProfileEffect.NavigateToLogin)
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        displayName = user?.fullName.orEmpty(),
                        handle = user?.handle.orEmpty(),
                        avatarInitials = user?.initials.orEmpty(),
                        isPremium = user?.isPremium == true,
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        appSettingsRepository.settings
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        settings = listOf(
                            ProfileSettingItem("sound_quality", "Ses kalitesi", settings.soundQuality, ProfileSettingIcon.SoundQuality),
                            ProfileSettingItem(
                                "offline_download",
                                "Çevrimdışı indirme",
                                if (settings.offlineDownloadEnabled) "Açık" else "Kapalı",
                                ProfileSettingIcon.OfflineDownload,
                            ),
                            ProfileSettingItem(
                                "notifications",
                                "Bildirimler",
                                if (settings.notificationsEnabled) "Açık" else "Kapalı",
                                ProfileSettingIcon.Notifications,
                            ),
                            ProfileSettingItem("privacy", "Gizlilik", iconKey = ProfileSettingIcon.Privacy),
                            ProfileSettingItem("help", "Yardım ve destek", iconKey = ProfileSettingIcon.Help),
                            ProfileSettingItem("logout", "Çıkış yap", iconKey = ProfileSettingIcon.Privacy),
                        ),
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
