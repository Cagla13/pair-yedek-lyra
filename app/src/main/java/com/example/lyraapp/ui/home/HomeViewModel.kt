package com.example.lyraapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.ui.favorites.FavoritesStorage
import com.example.lyraapp.ui.favorites.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    init {
        loadMockContent()
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        userName = user?.fullName.orEmpty(),
                        userInitials = user?.initials.orEmpty(),
                    )
                }
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.QuickPickClicked -> {
                // UI State güncelleniyor
                _uiState.update { current ->
                    val selected = current.quickPicks.find { it.id == intent.itemId }
                    val isFav = selected?.let { item ->
                        FavoritesStorage.savedSongsList.any { it.id == item.id }
                    } ?: false
                    current.copy(currentPlayingTrack = selected, isPlaying = true, isFavorite = isFav)
                }

                // EKSİK OLAN KISIM EKLENDİ: Detay sayfasına yönlendirme efekti tetikleniyor
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToDetails(intent.itemId))
                }
            }
            is HomeIntent.TrackClicked -> {
                // UI State güncelleniyor
                _uiState.update { current ->
                    val selected = current.recentlyPlayed.find { it.id == intent.itemId }
                        ?: current.customPlaylists.find { it.id == intent.itemId }
                    val isFav = selected?.let { item ->
                        FavoritesStorage.savedSongsList.any { it.id == item.id }
                    } ?: false
                    current.copy(currentPlayingTrack = selected, isPlaying = true, isFavorite = isFav)
                }

                // Aynı anda detay sayfasına yönlendirme efekti tetikleniyor
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToDetails(intent.itemId))
                }
            }
            HomeIntent.TogglePlayPause -> _uiState.update { it.copy(isPlaying = !it.isPlaying) }
            HomeIntent.ToggleFavorite -> {
                val currentTrack = _uiState.value.currentPlayingTrack
                if (currentTrack != null) {
                    val trackId = currentTrack.id
                    val isCurrentlyFav = FavoritesStorage.savedSongsList.any { it.id == trackId }

                    if (isCurrentlyFav) {
                        FavoritesStorage.savedSongsList.removeAll { it.id == trackId }
                    } else {
                        // Tasarım aşaması için şarkıların ID'lerine göre saniye içeren gerçekçi süreler atıyoruz
                        val trackDuration = when (trackId) {
                            "1" -> "3:34"
                            "2" -> "4:07"
                            "3" -> "3:43"
                            "4" -> "3:25"
                            "5" -> "4:29"
                            "6" -> "3:15"
                            "7" -> "4:02"
                            "8" -> "3:50"
                            "9" -> "2:58"
                            else -> "3:30"
                        }

                        val newFavorite = SongUiModel(
                            id = currentTrack.id,
                            title = currentTrack.title,
                            artist = currentTrack.subtitle ?: "Bilinmeyen Sanatçı",
                            duration = trackDuration,
                            isPlaying = false
                        )
                        FavoritesStorage.savedSongsList.add(newFavorite)
                    }
                    _uiState.update { it.copy(isFavorite = !isCurrentlyFav) }
                }
            }
            HomeIntent.ProfileClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToProfile)
            }
            HomeIntent.SeeAllRecentlyPlayedClicked -> {
                // İhtiyaca göre doldurulabilir
            }
            HomeIntent.MiniPlayerClicked -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToPlayer)
            }
        }
    }

    fun checkCurrentTrackFavoriteStatus() {
        val currentTrack = _uiState.value.currentPlayingTrack
        if (currentTrack != null) {
            val isFav = FavoritesStorage.savedSongsList.any { it.id == currentTrack.id }
            _uiState.update { it.copy(isFavorite = isFav) }
        }
    }

    private fun loadMockContent() {
        _uiState.update {
            HomeUiState(
                userName = "",
                userInitials = "",
                quickPicks = listOf(
                    PlayableItem("1", "Gece Sürüşü", "Sakin Ritmler", gradientIndex = 0),
                    PlayableItem("2", "Sabah Kahvesi", "Akustik", gradientIndex = 1),
                    PlayableItem("3", "Neon Sokaklar", "Şehir Işıkları", gradientIndex = 2),
                    PlayableItem("4", "Odaklan", "Lo-Fi", gradientIndex = 3),
                    PlayableItem("5", "Derin Mavi", "Okyanus", gradientIndex = 4),
                    PlayableItem("6", "Yaz Anıları", "Pop", gradientIndex = 5)
                ),
                recentlyPlayed = listOf(
                    PlayableItem("3", "Neon Sokaklar", "Şehir Işıkları"),
                    PlayableItem("5", "Derin Mavi", "Okyanus"),
                    PlayableItem("7", "Yıldız Tozu", "Polaris")
                ),
                customPlaylists = listOf(
                    PlayableItem("8", "Akustik Yolculuk", "Sakin Ritmler"),
                    PlayableItem("9", "Yeraltı Beats", "Lo-Fi Evreni")
                ),
                currentPlayingTrack = PlayableItem("3", "Neon Sokaklar", "Şehir Işıkları"),
                isFavorite = false,
                isPlaying = false,
                isLoading = false
            )
        }
    }
}