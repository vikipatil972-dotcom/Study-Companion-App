package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

data class StudyTrack(
    val title: String,
    val artist: String,
    val category: String, // Focus, Lofi, Night Study, Instrumental
    val durationSeconds: Int
)

@Composable
fun MusicPlayerComponent(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    trackProgress: Float,
    activeTrack: StudyTrack,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onSelectCategory: (String) -> Unit,
    selectedCategory: String,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Lofi", "Focus", "Night Study", "Instrumental")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CosmicSurface)
            .padding(16.dp)
    ) {
        // Categories row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Button(
                    onClick = { onSelectCategory(cat) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) CosmicTertiary else CosmicBackground,
                        contentColor = if (isSelected) CosmicBackground else CosmicOnSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("music_category_$cat")
                ) {
                    Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Track & Visualizer Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeTrack.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CosmicOnBackground
                    )
                )
                Text(
                    text = "${activeTrack.artist} • ${activeTrack.category}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CosmicOnSurface.copy(alpha = 0.6f)
                    )
                )
            }

            // Audio Wave Visualizer Component
            AudioWaveVisualizer(
                isPlaying = isPlaying,
                modifier = Modifier.size(width = 80.dp, height = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Playback progress bar
        val minutesPlayed = (activeTrack.durationSeconds * trackProgress / 60).toInt()
        val secondsPlayed = (activeTrack.durationSeconds * trackProgress % 60).toInt()
        val totalMinutes = activeTrack.durationSeconds / 60
        val totalSeconds = activeTrack.durationSeconds % 60

        Column {
            LinearProgressIndicator(
                progress = { trackProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CosmicTertiary,
                trackColor = CosmicBackground
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%02d:%02d", minutesPlayed, secondsPlayed),
                    fontSize = 10.sp,
                    color = CosmicOnSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = String.format("%02d:%02d", totalMinutes, totalSeconds),
                    fontSize = 10.sp,
                    color = CosmicOnSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onPrevTrack,
                modifier = Modifier.size(36.dp).testTag("music_prev_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = CosmicOnBackground
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CosmicTertiary)
                    .testTag("music_play_btn")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = CosmicBackground,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onNextTrack,
                modifier = Modifier.size(36.dp).testTag("music_next_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = CosmicOnBackground
                )
            }
        }
    }
}

@Composable
fun AudioWaveVisualizer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val barCount = 7
    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    
    // Animate wave height offsets if music is playing
    val barHeights = (0 until barCount).map { index ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 350 + index * 80, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
        } else {
            remember { mutableStateOf(0.15f) }
        }
    }

    Canvas(modifier = modifier) {
        val spacing = size.width / (barCount * 1.5f)
        val barWidth = spacing * 0.7f
        val maxCapY = size.height

        for (i in 0 until barCount) {
            val barHeight = maxCapY * barHeights[i].value
            val x = i * spacing * 1.4f + spacing * 0.3f
            val y = (maxCapY - barHeight) / 2f

            drawRoundRect(
                color = if (isPlaying) CosmicTertiary else CosmicOnSurface.copy(alpha = 0.3f),
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
