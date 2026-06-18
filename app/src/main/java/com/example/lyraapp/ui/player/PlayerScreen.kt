package com.example.lyraapp.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lyraapp.ui.icons.LyraIcons
import com.example.lyraapp.ui.player.components.PlayerAlbumArt
import com.example.lyraapp.ui.player.components.PlayerGradientBackground
import com.example.lyraapp.ui.theme.LyraAppTheme

@Composable
fun PlayerRoute(
    onNavigateBack: () -> Unit,
    onNavigateToBackgroundPreview: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PlayerEffect.NavigateBack -> onNavigateBack()
                PlayerEffect.NavigateToBackgroundPreview -> onNavigateToBackgroundPreview()
            }
        }
    }

    PlayerScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun PlayerScreen(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        PlayerGradientBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayerTopBar(
                sourceTitle = state.sourceTitle,
                onCollapse = { onIntent(PlayerIntent.Collapse) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            PlayerAlbumArt(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(320.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = state.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = { onIntent(PlayerIntent.ToggleFavorite) }) {
                    Icon(
                        imageVector = if (state.isFavorite) LyraIcons.Favorite else LyraIcons.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (state.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.White.copy(alpha = 0.8f)
                        },
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PlayerProgressSection(
                progressMs = state.progressMs,
                durationMs = state.durationMs,
                onSeek = { onIntent(PlayerIntent.SeekTo(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlayerControlsRow(
                shuffleEnabled = state.shuffleEnabled,
                repeatEnabled = state.repeatEnabled,
                isPlaying = state.isPlaying,
                onShuffle = { onIntent(PlayerIntent.ToggleShuffle) },
                onPrevious = { onIntent(PlayerIntent.SkipPrevious) },
                onTogglePlayPause = { onIntent(PlayerIntent.TogglePlayPause) },
                onNext = { onIntent(PlayerIntent.SkipNext) },
                onRepeat = { onIntent(PlayerIntent.ToggleRepeat) },
            )

            Spacer(modifier = Modifier.weight(1f))

            PlayerBottomActions(
                onBackgroundClick = { onIntent(PlayerIntent.OpenBackgroundPreview) },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlayerTopBar(
    sourceTitle: String,
    onCollapse: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Küçült",
                tint = Color.White,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ŞİMDİ ÇALIYOR",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.2.sp,
                color = Color.White.copy(alpha = 0.65f),
            )
            Text(
                text = sourceTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Daha fazla",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun PlayerProgressSection(
    progressMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
) {
    val progress = if (durationMs > 0L) progressMs.toFloat() / durationMs else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { fraction ->
                onSeek((durationMs * fraction).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.18f),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatPlaybackTime(progressMs),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
            Text(
                text = formatPlaybackTime(durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun PlayerControlsRow(
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    isPlaying: Boolean,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onShuffle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Karıştır",
                tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
            )
        }
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Önceki",
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
        }
        IconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier
                .size(72.dp)
                .shadow(12.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Icon(
                imageVector = if (isPlaying) LyraIcons.Pause else LyraIcons.PlayArrow,
                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Sonraki",
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
        }
        IconButton(onClick = onRepeat) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Tekrarla",
                tint = if (repeatEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun PlayerBottomActions(
    onBackgroundClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.Cast,
                contentDescription = "Cihaz",
                tint = Color.White.copy(alpha = 0.85f),
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onBackgroundClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "Arkaplan",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Kuyruk",
                tint = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

private fun formatPlaybackTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Preview
@Composable
private fun PlayerScreenPreview() {
    LyraAppTheme(darkTheme = true) {
        PlayerScreen(
            state = PlayerUiState(
                title = "Neon Sokaklar",
                artist = "Şehir Işıkları",
                sourceTitle = "Gece Vardiyası",
                isPlaying = true,
                isFavorite = true,
                progressMs = 93_000L,
                durationMs = 223_000L,
                repeatEnabled = true,
            ),
            onIntent = {},
        )
    }
}
