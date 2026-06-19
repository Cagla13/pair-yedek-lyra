package com.example.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadStaticProfileContent()
        observeCurrentUser()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.SettingsClicked -> viewModelScope.launch {
                _effect.send(ProfileEffect.ShowMessage("Ayarlar yakında eklenecek."))
            }
            is ProfileIntent.SettingClicked -> viewModelScope.launch {
                _effect.send(ProfileEffect.ShowMessage("Bu bölüm yakında eklenecek."))
            }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        displayName = user?.fullName ?: current.displayName,
                        handle = user?.handle ?: current.handle,
                        avatarInitials = user?.initials ?: current.avatarInitials,
                    )
                }
            }
        }
    }

    private fun loadStaticProfileContent() {
        _uiState.value = ProfileUiState(
            displayName = "",
            handle = "",
            isPremium = true,
            avatarInitials = "",
            stats = listOf(
                ProfileStat(value = "127", label = "Çalma listesi"),
                ProfileStat(value = "1.2B", label = "Takipçi"),
                ProfileStat(value = "348", label = "Takip"),
            ),
            settings = listOf(
                ProfileSettingItem(
                    id = "sound_quality",
                    title = "Ses kalitesi",
                    value = "Yüksek",
                    iconKey = ProfileSettingIcon.SoundQuality,
                ),
                ProfileSettingItem(
                    id = "offline_download",
                    title = "Çevrimdışı indirme",
                    value = "Açık",
                    iconKey = ProfileSettingIcon.OfflineDownload,
                ),
                ProfileSettingItem(
                    id = "notifications",
                    title = "Bildirimler",
                    iconKey = ProfileSettingIcon.Notifications,
                ),
                ProfileSettingItem(
                    id = "privacy",
                    title = "Gizlilik",
                    iconKey = ProfileSettingIcon.Privacy,
                ),
                ProfileSettingItem(
                    id = "help",
                    title = "Yardım ve destek",
                    iconKey = ProfileSettingIcon.Help,
                ),
            ),
        )
    }
}
