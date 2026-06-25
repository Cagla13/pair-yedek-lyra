package com.example.lyraapp.ui.playlist_detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PlaylistDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToPlayer.collect { onNavigateToPlayer() }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF140C0F)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    state.errorMessage?.let { message ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF140C0F))
                .clickable { viewModel.onEvent(PlaylistDetailContract.Event.RetryLoad) },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = message, color = Color.White)
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFF140C0F)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3A1C28),
                            Color(0xFF140C0F)
                        ),
                        startY = 0f,
                        endY = 1200f
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        viewModel.onEvent(PlaylistDetailContract.Event.OnBackClicked)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFB392D6),
                                            Color(0xFF7A54A6)
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = state.playlistTitle,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = state.playlistDescription,
                            fontSize = 14.sp,
                            color = Color(0xFFBDBDBD)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = state.playlistInfo,
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .shadow(
                                            elevation = 20.dp,
                                            shape = CircleShape,
                                            spotColor = MaterialTheme.colorScheme.primary
                                        )
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            viewModel.onEvent(PlaylistDetailContract.Event.OnPlayClicked)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    items(state.tracks) { track ->
                        TrackItem(
                            track = track,
                            onClick = { viewModel.onEvent(PlaylistDetailContract.Event.OnTrackClicked(track.id)) },
                            onLikeClick = { viewModel.onEvent(PlaylistDetailContract.Event.OnLikeClicked(track.id)) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TrackItem(
    track: PlaylistDetailContract.Track,
    onClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    val backgroundColor = if (track.isPlaying) Color(0xFF271D20) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(track.coverColor)),
            contentAlignment = Alignment.Center
        ) {
            if (track.isPlaying) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(3.dp).height(12.dp).background(Color.White))
                    Box(modifier = Modifier.width(3.dp).height(20.dp).background(Color.White))
                    Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color.White))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (track.isPlaying) MaterialTheme.colorScheme.primary else Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA)
            )
        }

        Text(
            text = track.duration,
            fontSize = 13.sp,
            color = Color(0xFFAAAAAA),
            modifier = Modifier.padding(end = 16.dp)
        )

        IconButton(
            onClick = onLikeClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (track.isLiked) MaterialTheme.colorScheme.primary else Color(0xFFAAAAAA),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color(0xFFAAAAAA),
            modifier = Modifier.size(20.dp)
        )
    }
}