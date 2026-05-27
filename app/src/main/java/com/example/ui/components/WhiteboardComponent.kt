package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class StrokeLine(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun WhiteboardComponent(
    strokes: List<StrokeLine>,
    onAddStroke: (StrokeLine) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedColor by remember { mutableStateOf(CosmicPrimary) }
    var strokeSize by remember { mutableStateOf(8f) }
    var showGridlines by remember { mutableStateOf(true) }

    val currentPoints = remember { mutableStateListOf<Offset>() }

    // Color Swatches
    val swatches = listOf(
        CosmicPrimary,
        CosmicSecondary,
        CosmicTertiary,
        AccentPink,
        AccentGreen,
        AccentOrange,
        Color.White
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicSecondary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        // Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Collaborative Board",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CosmicOnBackground
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showGridlines = !showGridlines },
                    modifier = Modifier.size(28.dp).testTag("board_grid_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Toggle Grid lines",
                        tint = if (showGridlines) CosmicTertiary else CosmicOnSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.2f), contentColor = AccentRed),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp).testTag("board_clear")
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear board",
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Whiteboard Canvas Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicBackground)
                .border(1.dp, CosmicSecondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .pointerInput(selectedColor, strokeSize) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(offset)
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                onAddStroke(
                                    StrokeLine(
                                        points = currentPoints.toList(),
                                        color = selectedColor,
                                        strokeWidth = strokeSize
                                    )
                                )
                            }
                            currentPoints.clear()
                        },
                        onDragCancel = {
                            currentPoints.clear()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newPoint = change.position
                            currentPoints.add(newPoint)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Grid lines if active
                if (showGridlines) {
                    val gridSpacing = 25f
                    val pathColor = CosmicOnSurface.copy(alpha = 0.05f)

                    // Vertical grid lines
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = pathColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += gridSpacing
                    }

                    // Horizontal grid lines
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = pathColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += gridSpacing
                    }
                }

                // 2. Draw historical strokes
                strokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.points[0].x, stroke.points[0].y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(
                                width = stroke.strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )
                    }
                }

                // 3. Draw active stroke draft
                if (currentPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPoints[0].x, currentPoints[0].y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = selectedColor,
                        style = Stroke(
                            width = strokeSize,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                }
            }

            if (strokes.isEmpty() && currentPoints.isEmpty()) {
                Text(
                    text = "Tap & drag here to sketch ideas, equations, or processes",
                    fontSize = 11.sp,
                    color = CosmicOnSurface.copy(alpha = 0.3f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tool Palette: Color Swatches & Stroke thickness
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Selector Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                swatches.forEach { swatch ->
                    val isSelected = selectedColor == swatch
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(50))
                            .background(swatch)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(50)
                            )
                            .pointerInput(swatch) {
                                detectDragGestures { _, _ -> }
                            }
                            .clickable { selectedColor = swatch }
                            .testTag("color_swatch_${swatch.value}")
                    )
                }
            }

            // Brush Size Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(4f, 8f, 15f).forEach { size ->
                    val isSelected = strokeSize == size
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CosmicTertiary else CosmicBackground)
                            .clickable { strokeSize = size }
                            .testTag("brush_size_$size"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((size * 0.8f).dp.coerceIn(4.dp, 16.dp))
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) CosmicBackground else CosmicOnSurface)
                        )
                    }
                }
            }
        }
    }
}
