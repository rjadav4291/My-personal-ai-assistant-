package com.chintu.ai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chintu.ai.core.common.AgentState

@Composable
fun FuturisticOrb(
    state: AgentState,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "OrbPulse")
    val scale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val color = when (state) {
        is AgentState.Ready -> Color(0xFF00E5FF)
        is AgentState.Listening -> Color(0xFF00FF66)
        is AgentState.Thinking -> Color(0xFFFFD600)
        is AgentState.Executing -> Color(0xFFFF9100)
        is AgentState.Speaking -> Color(0xFF7C4DFF)
        is AgentState.Error -> Color(0xFFFF1744)
        is AgentState.Offline -> Color(0xFF757575)
    }

    Canvas(modifier = modifier.size(190.dp)) {
        val centerOffset = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2) * 0.75f * scale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.5f), Color.Transparent),
                center = centerOffset,
                radius = radius * 1.35f
            ),
            center = centerOffset,
            radius = radius * 1.35f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0.25f)),
                center = centerOffset,
                radius = radius
            ),
            center = centerOffset,
            radius = radius
        )
    }
}
