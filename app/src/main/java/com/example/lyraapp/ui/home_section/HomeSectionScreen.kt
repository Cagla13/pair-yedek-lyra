@file:OptIn(ExperimentalFoundationApi::class)

package com.example.lyraapp.ui.home_section

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lyraapp.ui.home.PlayableItem
import com.example.lyraapp.ui.icons.LyraIcons
import com.example.lyraapp.ui.library.LibraryPlaylistItem

@Composable
fun HomeSectionRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylistDetail: (String) -> Unit,
    onNavigateToSongDetail: (String) -> Unit,
    viewModel: HomeSectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeSectionEffect.NavigateToPlayer -> onNavigateToPlayer()
                is HomeSectionEffect.NavigateToSongDetail -> onNavigateToSongDetail(effect.songId)
                is HomeSectionEffect.NavigateToPlaylistDetail -> onNavigateToPlaylistDetail(effect.playlistId)
            }
        }
    }

    HomeSectionScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onTrackClick = { viewModel.onIntent(HomeSectionIntent.TrackClicked(it)) },
        onTrackLongClick = { viewModel.onIntent(HomeSectionIntent.TrackLongClicked(it)) },
        onPlaylistClick = { viewModel.onIntent(HomeSectionIntent.PlaylistClicked(it)) },
        onRetry = { viewModel.onIntent(HomeSectionIntent.Retry) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSectionScreen(
    state: HomeSectionUiState,
    onNavigateBack: () -> Unit,
    onTrackClick: (String) -> Unit,
    onTrackLongClick: (String) -> Unit = {},
    onPlaylistClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.errorMessage)
                    Text(
                        text = "Tekrar dene",
                        modifier = Modifier.padding(top = 16.dp).clickable(onClick = onRetry),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            state.playlists.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.playlists, key = { it.id }) { playlist ->
                        FeaturedPlaylistRow(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist.id) },
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.tracks, key = { it.id }) { track ->
                        TrackRow(
                            track = track,
                            onClick = { onTrackClick(track.id) },
                            onLongClick = { onTrackLongClick(track.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: PlayableItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp),
    ) {
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = track.subtitle.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FeaturedPlaylistRow(
    playlist: LibraryPlaylistItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
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
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
