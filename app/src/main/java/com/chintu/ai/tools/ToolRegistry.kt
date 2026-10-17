package com.chintu.ai.tools

import androidx.compose.foundation.layout.width
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HeaderTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.width(220.dp),
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    ) {
        androidx.compose.material3.Tab(
            selected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) },
            text = {
                Text(
                    text = "UPGRADE",
                    maxLines = 1
                )
            }
        )
        androidx.compose.material3.Tab(
            selected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) },
            text = {
                Text(
                    text = "OTHER TAB",
                    maxLines = 1
                )
            }
        )
    }
}

class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): ToolContract? = tools[name]

    fun getAllTools(): List<ToolContract> = tools.values.toList()
}

interface ToolContract {
    val name: String
}