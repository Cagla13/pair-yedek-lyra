package com.example.lyraapp.ui.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lyraapp.ui.appearance.AppearanceSection
import com.example.lyraapp.ui.theme.LyraAppTheme
import com.example.lyraapp.ui.theme.ThemeViewModel

@Composable
fun ProfileRoute(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
                ProfileEffect.NavigateToEditProfile -> onNavigateToEditProfile()
            }
        }
    }

    ProfileScreen(
        state = uiState,
        isDarkTheme = isDarkTheme,
        onThemeSelected = themeViewModel::setDarkTheme,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    isDarkTheme: Boolean,
    onThemeSelected: (Boolean) -> Unit,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            item {
                ProfileHeader(onSettingsClick = { onIntent(ProfileIntent.SettingsClicked) })
            }

            item {
                ProfileUserSection(
                    displayName = state.displayName,
                    handle = state.handle,
                    isPremium = state.isPremium,
                    avatarInitials = state.avatarInitials,
                    stats = state.stats,
                )
            }

            item {
                Text(
                    text = "Profili düzenle",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIntent(ProfileIntent.EditProfileClicked) }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                AppearanceSection(
                    isDarkTheme = isDarkTheme,
                    onThemeSelected = onThemeSelected,
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(state.settings.size) { index ->
                val setting = state.settings[index]
                ProfileSettingRow(
                    item = setting,
                    onClick = { onIntent(ProfileIntent.SettingClicked(setting.id)) },
                )
                if (index < state.settings.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(start = 52.dp),
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Profil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ayarlar",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProfileUserSection(
    displayName: String,
    handle: String,
    isPremium: Boolean,
    avatarInitials: String,
    stats: List<ProfileStat>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = avatarInitials,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = buildString {
                append(handle)
                if (isPremium) {
                    append(" · Premium")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            stats.forEach { stat ->
                ProfileStatColumn(stat = stat)
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(stat: ProfileStat) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stat.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stat.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProfileSettingRow(
    item: ProfileSettingItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.iconKey.toIcon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        item.value?.let { value ->
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun ProfileSettingIcon.toIcon(): ImageVector = when (this) {
    ProfileSettingIcon.SoundQuality -> Icons.Outlined.GraphicEq
    ProfileSettingIcon.OfflineDownload -> Icons.Outlined.Download
    ProfileSettingIcon.Notifications -> Icons.Outlined.Notifications
    ProfileSettingIcon.Privacy -> Icons.Outlined.Lock
    ProfileSettingIcon.Help -> Icons.Outlined.HelpOutline
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        ProfileScreen(
            state = ProfileUiState(
                displayName = "Zeynep Kaya",
                handle = "@zeynepk",
                isPremium = true,
                avatarInitials = "ZK",
                stats = listOf(
                    ProfileStat("127", "Çalma listesi"),
                    ProfileStat("1.2B", "Takipçi"),
                    ProfileStat("348", "Takip"),
                ),
                settings = listOf(
                    ProfileSettingItem("sound_quality", "Ses kalitesi", "Yüksek", ProfileSettingIcon.SoundQuality),
                    ProfileSettingItem("notifications", "Bildirimler", iconKey = ProfileSettingIcon.Notifications),
                ),
            ),
            isDarkTheme = false,
            onThemeSelected = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        ProfileScreen(
            state = ProfileUiState(
                displayName = "Zeynep Kaya",
                handle = "@zeynepk",
                isPremium = true,
                avatarInitials = "ZK",
                stats = listOf(
                    ProfileStat("127", "Çalma listesi"),
                    ProfileStat("1.2B", "Takipçi"),
                    ProfileStat("348", "Takip"),
                ),
                settings = emptyList(),
            ),
            isDarkTheme = true,
            onThemeSelected = {},
            onIntent = {},
        )
    }
}
