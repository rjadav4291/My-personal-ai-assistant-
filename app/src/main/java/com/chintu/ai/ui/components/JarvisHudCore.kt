package com.chintu.ai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chintu.ai.core.common.AgentState

@Composable
fun JarvisHudCore(state: AgentState, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_hud")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_outer"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_inner"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_core"
    )

    val coreColor = when (state) {
        is AgentState.Ready -> Color(0xFF00E5FF)
        is AgentState.Listening -> Color(0xFF00FF66)
        is AgentState.Thinking -> Color(0xFFFFD600)
        is AgentState.Speaking -> Color(0xFFB388FF)
        is AgentState.Offline -> Color(0xFF78909C)
        is AgentState.Error -> Color(0xFFFF1744)
    }

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = (size.minDimension / 2) * 0.88f

            // 1. Outer Ring Segments
            drawArc(
                color = coreColor.copy(alpha = 0.35f),
                startAngle = rotationAngle,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = coreColor.copy(alpha = 0.35f),
                startAngle = rotationAngle + 180f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Inner Counter Ring
            val innerRadius = baseRadius * 0.76f
            drawArc(
                color = coreColor.copy(alpha = 0.6f),
                startAngle = counterRotation,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Central Glowing Reactor Core
            val coreRadius = baseRadius * 0.52f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreColor, coreColor.copy(alpha = 0.25f), Color.Transparent),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )
        }

        Text(
            text = "AI CORE",
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
