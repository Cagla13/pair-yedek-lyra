package com.example.lyraapp.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EditProfileRoute(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditProfileEffect.NavigateBack -> onNavigateBack()
                is EditProfileEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    EditProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onIntent: (EditProfileIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profili düzenle") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(EditProfileIntent.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { onIntent(EditProfileIntent.FirstNameChanged(it)) },
                label = { Text("Ad") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { onIntent(EditProfileIntent.LastNameChanged(it)) },
                label = { Text("Soyad") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.birthDate,
                onValueChange = { onIntent(EditProfileIntent.BirthDateChanged(it)) },
                label = { Text("Doğum tarihi (YYYY-MM-DD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onIntent(EditProfileIntent.Save) },
                enabled = state.isSaveEnabled && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                } else {
                    Text("Kaydet")
                }
            }
        }
    }
}
