package com.example.lyraapp.ui.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.lyraapp.ui.theme.DarkTertiary
import com.example.lyraapp.ui.theme.DarkTertiaryContainer

object PlayerVisuals {

    val warmGradientColors = listOf(
        Color(0xFF8B5A2B),
        Color(0xFF5C3A1E),
        Color(0xFF1A0F0A),
    )

    val notificationGradientColors = listOf(
        Color(0xFF9A6B3D),
        Color(0xFF6B4423),
        Color(0xFF4A2E18),
    )
}

@Composable
fun PlayerGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = PlayerVisuals.warmGradientColors,
) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(colors),
        ),
    )
}

@Composable
fun PlayerAlbumArt(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(DarkTertiaryContainer, DarkTertiary),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.04f
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension * 0.42f
            val ringCount = 5
            repeat(ringCount) { index ->
                val radius = maxRadius * (index + 1) / ringCount
                drawArc(
                    color = Color.White.copy(alpha = 0.18f + index * 0.06f),
                    startAngle = 200f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    }
}

@Composable
fun PlayerAlbumArtThumbnail(
    modifier: Modifier = Modifier,
) {
    PlayerAlbumArt(modifier = modifier.size(48.dp))
}
