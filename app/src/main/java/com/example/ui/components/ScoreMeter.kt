package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AiScoreOrange
import com.example.ui.theme.AiScoreRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HumanScoreEmerald
import com.example.ui.theme.NavyBorder

@Composable
fun CircularScoreGauge(
    aiScore: Int,
    humanScore: Int,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 14.dp
) {
    val animatedAi by animateFloatAsState(
        targetValue = aiScore / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "aiGauge"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.toPx() - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = NavyBorder.copy(alpha = 0.5f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Human Arc (Left portion)
            val totalSweep = 270f
            val aiSweep = totalSweep * animatedAi
            val humanSweep = totalSweep * (1f - animatedAi)

            // AI Arc
            if (aiSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(AiScoreRed, AiScoreOrange, AiScoreRed)
                    ),
                    startAngle = 135f,
                    sweepAngle = aiSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$aiScore%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (aiScore >= 50) AiScoreOrange else HumanScoreEmerald
            )
            Text(
                text = "Estimated AI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DualLikelihoodBar(
    aiScore: Int,
    humanScore: Int,
    modifier: Modifier = Modifier
) {
    val animatedAiFraction by animateFloatAsState(
        targetValue = aiScore / 100f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "aiFraction"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AiScoreOrange)
                )
                Text(
                    text = " AI-like: $aiScore%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(HumanScoreEmerald)
                )
                Text(
                    text = " Human-like: $humanScore%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-color segmented track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (aiScore > 0) {
                    Box(
                        modifier = Modifier
                            .weight(animatedAiFraction.coerceAtLeast(0.02f))
                            .height(14.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AiScoreRed, AiScoreOrange)
                                )
                            )
                    )
                }
                if (humanScore > 0) {
                    Box(
                        modifier = Modifier
                            .weight((1f - animatedAiFraction).coerceAtLeast(0.02f))
                            .height(14.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(HumanScoreEmerald, CyanAccent)
                                )
                            )
                    )
                }
            }
        }
    }
}
