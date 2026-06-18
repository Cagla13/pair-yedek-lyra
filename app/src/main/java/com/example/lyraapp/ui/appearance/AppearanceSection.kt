package com.example.lyraapp.ui.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lyraapp.ui.theme.LyraAppTheme

@Composable
fun AppearanceSection(
    isDarkTheme: Boolean,
    onThemeSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Görünüm",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 8.dp),
    )

    AppearanceToggle(
        isDarkTheme = isDarkTheme,
        onThemeSelected = onThemeSelected,
    )
}

@Composable
fun AppearanceToggle(
    isDarkTheme: Boolean,
    onThemeSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(4.dp),
    ) {
        AppearanceToggleOption(
            label = "Açık",
            icon = Icons.Outlined.LightMode,
            selected = !isDarkTheme,
            onClick = { onThemeSelected(false) },
            modifier = Modifier.weight(1f),
        )
        AppearanceToggleOption(
            label = "Koyu",
            icon = Icons.Outlined.DarkMode,
            selected = isDarkTheme,
            onClick = { onThemeSelected(true) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AppearanceToggleOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Preview
@Composable
private fun AppearanceToggleDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        AppearanceSection(
            isDarkTheme = true,
            onThemeSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun AppearanceToggleLightPreview() {
    LyraAppTheme(darkTheme = false) {
        AppearanceSection(
            isDarkTheme = false,
            onThemeSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
