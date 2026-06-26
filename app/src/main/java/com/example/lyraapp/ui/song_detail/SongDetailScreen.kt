package com.example.lyraapp.ui.song_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SongDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: SongDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SongDetailEffect.NavigateToPlayer -> onNavigateToPlayer()
                is SongDetailEffect.ShowError -> Unit
            }
        }
    }

    SongDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onPlay = { viewModel.onIntent(SongDetailIntent.Play) },
        onRetry = { viewModel.onIntent(SongDetailIntent.Retry) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    state: SongDetailUiState,
    onNavigateBack: () -> Unit,
    onPlay: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şarkı detayı") },
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
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                ) {
                    Text(state.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.artist, style = MaterialTheme.typography.titleMedium)
                    if (state.album.isNotBlank()) {
                        Text("Albüm: ${state.album}", style = MaterialTheme.typography.bodyLarge)
                    }
                    Text("Süre: ${state.duration}", style = MaterialTheme.typography.bodyLarge)
                    if (state.mimeType.isNotBlank()) {
                        Text("Format: ${state.mimeType}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) {
                        Text("Oynat")
                    }
                }
            }
        }
    }
}
