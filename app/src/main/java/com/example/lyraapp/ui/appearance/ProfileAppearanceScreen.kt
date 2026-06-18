package com.example.lyraapp.ui.appearance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lyraapp.ui.theme.ThemeViewModel

/**
 * Profil ekranındaki "Görünüm" bölümünün bağımsız önizlemesi.
 * Arkadaşların profil ekranına [AppearanceSection] composable'ı entegre edebilir.
 */
@Composable
fun ProfileAppearanceRoute(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()

    ProfileAppearanceScreen(
        isDarkTheme = isDarkTheme,
        onThemeSelected = themeViewModel::setDarkTheme,
        modifier = modifier,
    )
}

@Composable
fun ProfileAppearanceScreen(
    isDarkTheme: Boolean,
    onThemeSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            AppearanceSection(
                isDarkTheme = isDarkTheme,
                onThemeSelected = onThemeSelected,
            )
        }
    }
}
