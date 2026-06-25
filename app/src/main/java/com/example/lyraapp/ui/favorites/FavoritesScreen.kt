package com.example.lyraapp.ui.favorites

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Ekran her görünür olduğunda depodaki dinamik verileri sıcağı sıcağına tazeler
    LaunchedEffect(key1 = true) {
        viewModel.refreshFavorites()
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is FavoritesContract.SideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD1D1), Color(0xFFFCA1A1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFF5A001B),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = "Beğenilen Şarkılar",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.totalDurationText,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.onIntent(FavoritesContract.Intent.OnPlayAllClick) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB2D1)),
                        shape = RoundedCornerShape(24.dp),
                        enabled = state.songs.isNotEmpty() // Liste boşken buton pasif olsun
                    ) {
                        Text("▶ Çal", color = Color(0xFF5A001B), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        "⇄",
                        fontSize = 24.sp,
                        modifier = Modifier.clickable(enabled = state.songs.isNotEmpty()) {
                            viewModel.onIntent(FavoritesContract.Intent.OnShuffleClick)
                        }
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("↓", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // DİNAMİK BOŞ DURUM (EMPTY STATE) KONTROLÜ
            if (state.songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz beğenilen şarkı yok.\nAna sayfadaki parçaları kalpleyerek başlayın!",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(state.songs) { song ->
                    SongListItem(
                        song = song,
                        onSongClick = { viewModel.onIntent(FavoritesContract.Intent.OnSongClick(song.id)) },
                        onHeartClick = { viewModel.onIntent(FavoritesContract.Intent.OnRemoveFromFavorites(song.id)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SongListItem(
    song: SongUiModel,
    onSongClick: () -> Unit,
    onHeartClick: () -> Unit
) {
    val itemBg = if (song.isPlaying) Color.Gray.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(itemBg)
            .clickable { onSongClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (song.isPlaying) {
                Text("║║", color = Color(0xFFFFB2D1), fontWeight = FontWeight.Bold)
            } else {
                Text("♫", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (song.isPlaying) Color(0xFFFFB2D1) else Color.Unspecified
            )
            Text(
                text = song.artist,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(text = song.duration, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFFFB2D1),
            modifier = Modifier
                .size(20.dp)
                .clickable { onHeartClick() }
        )

        Spacer(modifier = Modifier.width(16.dp))
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
    }
}