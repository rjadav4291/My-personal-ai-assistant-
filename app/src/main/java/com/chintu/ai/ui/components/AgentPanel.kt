package com.chintu.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AgentPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF080C14).copy(alpha = 0.9f))
            .padding(12.dp)
    ) {
        Text(
            text = "ACTIVE AGENTS",
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val agents = listOf(
            Triple("COGNITIVE ORCHESTRATOR", "Status: Orchestrating", Color(0xFF00E5FF)),
            Triple("WEB RESEARCHER", "Status: Fetching live data", Color(0xFF00FF66)),
            Triple("CODING SPECIALIST", "Status: Ready", Color(0xFFFF9100)),
            Triple("VOICE & SYSTEM AGENT", "Status: Listening", Color(0xFF7C4DFF))
        )

        agents.forEach { (name, status, accent) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .cyberBorder(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101622))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glowing placeholder for avatar holograms
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accent.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                            .border(1.dp, accent, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = status,
                            color = accent,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
