package com.example.lyraapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lyraapp.ui.icons.LyraIcons
import com.example.lyraapp.ui.library.LibraryPlaylistItem


@Composable
fun HomeRoute(
    onNavigateToProfile: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToRecentlyPlayed: () -> Unit,
    onNavigateToForYou: () -> Unit,
    onNavigateToRecommendations: () -> Unit,
    onNavigateToFeaturedPlaylists: () -> Unit,
    onNavigateToPlaylistDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.onHomeResumed()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToProfile -> onNavigateToProfile()
                HomeEffect.NavigateToPlayer -> onNavigateToPlayer()
                HomeEffect.NavigateToRecentlyPlayed -> onNavigateToRecentlyPlayed()
                HomeEffect.NavigateToForYou -> onNavigateToForYou()
                HomeEffect.NavigateToRecommendations -> onNavigateToRecommendations()
                HomeEffect.NavigateToFeaturedPlaylists -> onNavigateToFeaturedPlaylists()
                is HomeEffect.NavigateToPlaylistDetail -> onNavigateToPlaylistDetail(effect.playlistId)
                is HomeEffect.ShowNotification -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeScreen(
            state = uiState,
            onIntent = viewModel::onIntent,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            state.currentPlayingTrack?.let { track ->
                MiniPlayer(
                    track = track,
                    isPlaying = state.isPlaying,
                    isFavorite = state.isFavorite,
                    onBarClick = { onIntent(HomeIntent.MiniPlayerClicked) },
                    onTogglePlay = { onIntent(HomeIntent.TogglePlayPause) },
                    onToggleFavorite = { onIntent(HomeIntent.ToggleFavorite) },
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading && state.quickPicks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.errorMessage != null && state.quickPicks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tekrar dene",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onIntent(HomeIntent.RetryLoad) },
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Üst Başlık ve Profil Alanı
            item {
                HomeHeader(
                    userName = state.userName,
                    userInitials = state.userInitials,
                    onProfileClick = { onIntent(HomeIntent.ProfileClicked) }
                )
            }

            // 2. Hızlı Seçimler Grid Yapısı
            item {
                Text(
                    text = "Ne dinlemek istersin?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                )
                QuickPicksGrid(items = state.quickPicks, onItemClick = { onIntent(HomeIntent.QuickPickClicked(it)) })
            }

            // 3. Son Çalınanlar
            item {
                SectionHeader(
                    title = "Son çalınanlar",
                    onSeeAllClick = { onIntent(HomeIntent.SeeAllRecentlyPlayedClicked) },
                )
                HorizontalTrackList(items = state.recentlyPlayed, onItemClick = { onIntent(HomeIntent.TrackClicked(it)) })
            }

            // 4. Senin İçin Müzikler (for-you)
            item {
                SectionHeader(
                    title = "Senin için müzikler",
                    onSeeAllClick = { onIntent(HomeIntent.SeeAllForYouClicked) },
                )
                HorizontalTrackList(
                    items = state.forYouMusic,
                    onItemClick = { onIntent(HomeIntent.TrackClicked(it)) },
                )
            }

            // 5. Sana Önerilenler (recommendations)
            item {
                SectionHeader(
                    title = "Sana önerilenler",
                    onSeeAllClick = { onIntent(HomeIntent.SeeAllRecommendationsClicked) },
                )
                HorizontalTrackList(
                    items = state.recommendations,
                    onItemClick = { onIntent(HomeIntent.TrackClicked(it)) },
                )
            }

            // 6. Öne çıkan çalma listeleri
            if (state.featuredPlaylists.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Öne çıkan listeler",
                        onSeeAllClick = { onIntent(HomeIntent.SeeAllFeaturedPlaylistsClicked) },
                    )
                    FeaturedPlaylistsRow(
                        playlists = state.featuredPlaylists,
                        onPlaylistClick = { onIntent(HomeIntent.FeaturedPlaylistClicked(it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    userInitials: String,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "İyi akşamlar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        // Profil İkonu (Mock Tasarımdaki Pembe ZK gibi yuvarlak alan)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userInitials.ifBlank { userName.take(2).uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun QuickPicksGrid(items: List<PlayableItem>, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
        val chunked = items.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    QuickPickCard(item = item, modifier = Modifier.weight(1f), onClick = { onItemClick(item.id) })
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickPickCard(item: PlayableItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val gradients = listOf(
        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
        listOf(Color(0xFFED213A), Color(0xFF93291E)),
        listOf(Color(0xFF11998e), Color(0xFF38ef7d))
    )
    val currentGradient = gradients[item.gradientIndex % gradients.size]

    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(currentGradient))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = "Tümü",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onSeeAllClick)
        )
    }
}

@Composable
private fun HorizontalTrackList(items: List<PlayableItem>, onItemClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onItemClick(item.id) }
            ) {
                // Mock Tasarımdaki Renkli Albüm Kapağı Alanı
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF434343), Color(0xFF000000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LyraIcons.Waveform,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                item.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedPlaylistsRow(
    playlists: List<LibraryPlaylistItem>,
    onPlaylistClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(playlists.size) { index ->
            val playlist = playlists[index]
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onPlaylistClick(playlist.id) },
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(playlist.gradientStartColor),
                                    Color(playlist.gradientEndColor),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = LyraIcons.Waveform,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MiniPlayer(
    track: PlayableItem,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onBarClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onBarClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = LyraIcons.Waveform, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = track.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    track.subtitle?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) LyraIcons.Favorite else LyraIcons.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) LyraIcons.Pause else LyraIcons.PlayArrow,
                    contentDescription = "Oynat/Durdur"
                )
            }
        }
    }
}