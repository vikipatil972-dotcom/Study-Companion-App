package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConceptAnimator(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Pendulum Physics, 1 = Binary search tree

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Simulation Lab",
                        tint = CosmicTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Concept Animation Labs",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CosmicOnBackground
                        )
                    )
                }
                
                // Toggle tabs
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicBackground)
                        .padding(2.dp)
                ) {
                    Text(
                        text = "Pendulum",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTab == 0) CosmicBackground else CosmicOnSurface.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == 0) CosmicTertiary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "BST Search",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTab == 1) CosmicBackground else CosmicOnSurface.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == 1) CosmicTertiary else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "SimSwitcher"
            ) { tab ->
                when (tab) {
                    0 -> PendulumSimulation()
                    1 -> BinarySearchTreeSimulation()
                }
            }
        }
    }
}

@Composable
fun PendulumSimulation() {
    var length by remember { mutableStateOf(120f) } // 50 to 200
    var gravity by remember { mutableStateOf(9.8f) } // 2 to 20
    var isRunning by remember { mutableStateOf(true) }
    var time by remember { mutableStateOf(0f) }

    // Constants
    val amplitude = 45f // angle in degrees

    // Handle time ticking
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                time += 0.05f
                delay(16) // ~60fps
            }
        }
    }

    val omega = remember(gravity, length) { kotlin.math.sqrt(gravity / (length / 10f)) }
    val angleRad = remember(time, omega) { 
        val angleDeg = amplitude * cos(omega * time)
        Math.toRadians(angleDeg.toDouble()).toFloat()
    }

    val period = remember(gravity, length) {
        2 * Math.PI * kotlin.math.sqrt((length / 10f) / gravity)
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicBackground)
                .border(1.dp, CosmicSecondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = 20f
                val px = cx + length * sin(angleRad)
                val py = cy + length * cos(angleRad)

                // Draw pivot plate
                drawCircle(color = CosmicSecondary.copy(alpha = 0.6f), radius = 6f, center = Offset(cx, cy))
                
                // Draw string line
                drawLine(
                    color = CosmicOnBackground.copy(alpha = 0.4f),
                    start = Offset(cx, cy),
                    end = Offset(px, py),
                    strokeWidth = 3f
                )

                // Draw bob
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentPink, CosmicSecondary),
                        center = Offset(px, py),
                        radius = 16f
                    ),
                    radius = 16f,
                    center = Offset(px, py)
                )

                // Draw trajectory arc line helper
                val trajectoryWidth = length * sin(Math.toRadians(amplitude.toDouble()).toFloat())
                val trajectoryHeight = length * (1f - cos(Math.toRadians(amplitude.toDouble()).toFloat()))
                
                // Draw live displacement graphing at bottom
                val graphY = size.height - 35f
                val graphWidth = size.width
                val points = 100
                val path = Path()
                path.moveTo(0f, graphY)
                for (i in 0..points) {
                    val x = (i.toFloat() / points) * graphWidth
                    val sineVal = cos(omega * (time - (points - i) * 0.1f))
                    val y = graphY + sineVal * 20f
                    path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = CosmicTertiary.copy(alpha = 0.5f),
                    style = Stroke(width = 4f)
                )

                // Center displacement baseline
                drawLine(
                    color = CosmicOnBackground.copy(alpha = 0.1f),
                    start = Offset(0f, graphY),
                    end = Offset(graphWidth, graphY),
                    strokeWidth = 2f
                )

                // Live pointer on graph
                drawCircle(
                    color = CosmicTertiary,
                    radius = 6f,
                    center = Offset(graphWidth, graphY + cos(omega * time) * 20f)
                )
            }

            // Live Values Floating Label
            Text(
                text = String.format("Period T: %.2fs  |  f: %.2fHz", period, 1f / period),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CosmicTertiary
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(CosmicSurface.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sliders & Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("Pendulum Length: %.0f cm", length),
                    fontSize = 11.sp,
                    color = CosmicOnSurface,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = length,
                    onValueChange = { length = it },
                    valueRange = 60f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = CosmicTertiary,
                        activeTrackColor = CosmicTertiary,
                        inactiveTrackColor = CosmicOnBackground.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.height(24.dp).testTag("length_slider")
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("Gravity g: %.1f m/s²", gravity),
                    fontSize = 11.sp,
                    color = CosmicOnSurface,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = gravity,
                    onValueChange = { gravity = it },
                    valueRange = 2.0f..20.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = CosmicSecondary,
                        activeTrackColor = CosmicSecondary,
                        inactiveTrackColor = CosmicOnBackground.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.height(24.dp).testTag("gravity_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    time = 0f
                },
                modifier = Modifier.size(36.dp).testTag("reset_pendulum")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset time",
                    tint = CosmicOnSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) AccentOrange else CosmicTertiary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp).testTag("toggle_pendulum")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                    contentDescription = "Stop",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isRunning) "Pause Animation" else "Resume Animation",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicBackground
                )
            }
        }
    }
}

@Composable
fun BinarySearchTreeSimulation() {
    var searchNodeValue by remember { mutableStateOf(5) }
    var activeNodeId by remember { mutableStateOf(0) } // 0 = none, 1-7 layout node IDs
    var traversalPath by remember { mutableStateOf(listOf<Int>()) }
    var isSearching by remember { mutableStateOf(false) }

    // Simulates a BST search traversal visually step by step
    LaunchedEffect(isSearching) {
        if (isSearching) {
            traversalPath = emptyList()
            // BST structure: Root=50, L=25, R=75, LL=12, LR=37, RL=62, RR=87
            val bstSteps = when (searchNodeValue) {
                12 -> listOf(50, 25, 12)
                37 -> listOf(50, 25, 37)
                62 -> listOf(50, 75, 62)
                87 -> listOf(50, 75, 87)
                25 -> listOf(50, 25)
                75 -> listOf(50, 75)
                50 -> listOf(50)
                else -> listOf(50)
            }

            for (step in bstSteps) {
                activeNodeId = step
                traversalPath = traversalPath + step
                delay(800) // Highlight step duration
            }
            delay(1000)
            isSearching = false
            activeNodeId = 0
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicBackground)
                .border(1.dp, CosmicSecondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val dY = 50f
                val dX1 = 110f
                val dX2 = 55f

                // Static coordinates
                // Level 0: 50
                val p50 = Offset(cx, 25f)
                // Level 1: 25, 75
                val p25 = Offset(cx - dX1, 25f + dY)
                val p75 = Offset(cx + dX1, 25f + dY)
                // Level 2: 12, 37, 62, 87
                val p12 = Offset(cx - dX1 - dX2, 25f + dY * 2)
                val p37 = Offset(cx - dX1 + dX2, 25f + dY * 2)
                val p62 = Offset(cx + dX1 - dX2, 25f + dY * 2)
                val p87 = Offset(cx + dX1 + dX2, 25f + dY * 2)

                // Helper lambda to draw connection lines
                fun drawJoint(from: Offset, to: Offset) {
                    drawLine(
                        color = CosmicOnBackground.copy(alpha = 0.2f),
                        start = from,
                        end = to,
                        strokeWidth = 3f
                    )
                }

                // Draw joints
                drawJoint(p50, p25)
                drawJoint(p50, p75)
                drawJoint(p25, p12)
                drawJoint(p25, p37)
                drawJoint(p75, p62)
                drawJoint(p75, p87)
            }

            // Draw Nodes Overlay
            Box(modifier = Modifier.fillMaxSize()) {
                val values = listOf(
                    NodePos(50, 0.5f, 0.14f),
                    NodePos(25, 0.26f, 0.42f),
                    NodePos(75, 0.74f, 0.42f),
                    NodePos(12, 0.12f, 0.70f),
                    NodePos(37, 0.40f, 0.70f),
                    NodePos(62, 0.60f, 0.70f),
                    NodePos(87, 0.88f, 0.70f)
                )

                values.forEach { node ->
                    Box(
                        modifier = Modifier
                            .align(BiasAlignment(node.biasX, node.biasY))
                            .size(28.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                color = if (activeNodeId == node.value) {
                                    AccentGreen
                                } else if (traversalPath.contains(node.value)) {
                                    CosmicSecondary
                                } else {
                                    CosmicSurface
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (activeNodeId == node.value) AccentGreen else CosmicSecondary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${node.value}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeNodeId == node.value || traversalPath.contains(node.value)) CosmicBackground else CosmicOnSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Selectors
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Target Search Node:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = CosmicOnSurface
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val targets = listOf(12, 37, 50, 62, 87)
                targets.forEach { target ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (searchNodeValue == target) CosmicTertiary else CosmicSurface)
                            .clickable { searchNodeValue = target }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$target",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (searchNodeValue == target) CosmicBackground else CosmicOnSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Traverse visualizer logs
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = if (isSearching) "Searching BST..." else "Search complete!",
                    fontSize = 10.sp,
                    color = if (isSearching) CosmicTertiary else AccentGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Path: " + traversalPath.joinToString(" -> "),
                    fontSize = 11.sp,
                    color = CosmicOnSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }

            Button(
                onClick = { isSearching = true },
                enabled = !isSearching,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CosmicTertiary,
                    disabledContainerColor = CosmicSurface
                ),
                contentPadding = PaddingValues(horizontal = 14.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp).testTag("bst_traverse_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Search",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Search Step-by-Step", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Visual bias alignment model
data class NodePos(val value: Int, val x: Float, val y: Float) {
    val biasX get() = x * 2f - 1f
    val biasY get() = y * 2f - 1f
}
