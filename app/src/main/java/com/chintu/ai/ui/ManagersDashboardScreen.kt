package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AgentState { IDLE, PLANNING, EXECUTING, FINISHED }

data class AgentNode(
    val id: String,
    val name: String,
    val role: String,
    val state: AgentState,
    val assignedTools: List<String>,
    val subAgents: List<AgentNode> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagersDashboardScreen(onBackClick: () -> Unit = {}) {
    var agentTree by remember {
        mutableStateOf(
            AgentNode(
                id = "boss-1",
                name = "CHINTU",
                role = "Root Boss Agent",
                state = AgentState.PLANNING,
                assignedTools = listOf("Orchestrator", "TaskDelegator"),
                subAgents = listOf(
                    AgentNode(
                        id = "mgr-1",
                        name = "Code Manager",
                        role = "Architecture & Testing",
                        state = AgentState.EXECUTING,
                        assignedTools = listOf("RepoAction", "GitCommit"),
                        subAgents = listOf(
                            AgentNode(
                                id = "sub-1",
                                name = "Syntax Checker",
                                role = "Sub-Agent Worker",
                                state = AgentState.EXECUTING,
                                assignedTools = listOf("CompilerCheck")
                            )
                        )
                    ),
                    AgentNode(
                        id = "mgr-2",
                        name = "Research Manager",
                        role = "Web & Knowledge Fetch",
                        state = AgentState.IDLE,
                        assignedTools = listOf("WebFetch", "Summarizer")
                    )
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Managers Dashboard", color = Color.Cyan) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF10141E))
            )
        },
        containerColor = Color(0xFF0B0E14)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Hierarchical Agent Network",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            item {
                AgentCard(node = agentTree, depth = 0)
            }
        }
    }
}

@Composable
fun AgentCard(node: AgentNode, depth: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp, top = 6.dp, bottom = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B26)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = node.name, color = Color.Cyan, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = node.state.name,
                    color = if (node.state == AgentState.EXECUTING) Color.Green else Color.Yellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Role: ${node.role}", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Tools: ${node.assignedTools.joinToString(", ")}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )

            if (node.subAgents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                node.subAgents.forEach { child ->
                    AgentCard(node = child, depth = depth + 1)
                }
            }
        }
    }
}
