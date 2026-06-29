package com.example.lyraapp.ui.player

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lyraapp.ui.player.components.LyraMiniPlayer

@Composable
fun LyraPlaybackBar(
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaybackBarViewModel = hiltViewModel(
        LocalContext.current as ComponentActivity,
    ),
) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val track = playbackState.track

    if (!playbackState.isVisible || track == null) return

    LyraMiniPlayer(
        track = track,
        isPlaying = playbackState.isPlaying,
        progressMs = playbackState.progressMs,
        durationMs = playbackState.durationMs,
        isPlayingAd = playbackState.isPlayingAd,
        adTitle = playbackState.adTitle,
        onBarClick = onNavigateToPlayer,
        onTogglePlayPause = viewModel::togglePlayPause,
        modifier = modifier,
    )
}
