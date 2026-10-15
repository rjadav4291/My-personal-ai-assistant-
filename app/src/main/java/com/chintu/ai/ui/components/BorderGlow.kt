package com.chintu.ai.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.cyberBorder(): Modifier = this.then(
    Modifier.border(
        width = 1.5.dp,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00E5FF), // Cyan
                Color(0xFF00B0FF), // Soft Blue
                Color(0x3300E5FF)  // Transparent Cyan
            )
        ),
        shape = RoundedCornerShape(8.dp)
    )
)
