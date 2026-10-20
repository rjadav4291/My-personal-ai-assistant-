**

package com.chintu.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple data class representing an agent node in a hierarchical tree.
 */
data class AgentNode(
    val name: String,
    val children: List<AgentNode> = emptyList()
)

/**
 * Top‑level composable that renders the managers' dashboard.
 *
 * @param onAgentClick Called when an agent node is tapped – the caller can decide what to do.
 */
@Composable
fun ManagersDashboardScreen(
    onAgentClick: (AgentNode) -> Unit = {}
) {
    // In a real app this would come from a ViewModel or repository.
    val agents = remember { sampleAgents() }

    AgentTreeList(
        nodes = agents,
        onAgentClick = onAgentClick
    )
}

/**
 * Recursively renders a list of [AgentNode] items as an indented tree.
 *
 * @param nodes The list of nodes at the current hierarchy level.
 * @param level Current depth – used for indentation.
 * @param onAgentClick Propagated click handler.
 */
@Composable
private fun AgentTreeList(
    nodes: List<AgentNode>,
    onAgentClick: (AgentNode) -> Unit,
    level: Int = 0
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = (level * 16).dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(nodes) { node ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onAgentClick(node) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = node.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (node.children.isNotEmpty()) {
                    AgentTreeList(
                        nodes = node.children,
                        onAgentClick = onAgentClick,
                        level = level + 1
                    )
                }
            }
        }
    }
}

/**
 * Dummy data used for preview / quick testing.
 */
private fun sampleAgents(): List<AgentNode> = listOf(
    AgentNode(
        name = "Root Manager",
        children = listOf(
            AgentNode(
                name = "Team Alpha",
                children = listOf(
                    AgentNode(name = "Agent A1"),
                    AgentNode(name = "Agent A2")
                )
            ),
            AgentNode(
                name = "Team Beta",
                children = listOf(
                    AgentNode(name = "Agent B1"),
                    AgentNode(name = "Agent B2"),
                    AgentNode(
                        name = "Sub‑Team B‑X",
                        children = listOf(
                            AgentNode(name = "Agent BX1"),
                            AgentNode(name = "Agent BX2")
                        )
                    )
                )
            )
        )
    )
)


---

**DECISION:** UPDATE  
**FILE_PATH:** app/src/main/java/com/chintu/ai/MainActivity.kt  
**CODE:**

package com.chintu.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.chintu.ai.ui.ManagersDashboardScreen
import com.chintu.ai.ui.WelcomeScreen   // assume this existing screen
import com.chintu.ai.ui.theme.AiTheme   // assume theme package

/**
 * Central activity that hosts the Jetpack Compose navigation graph.
 *
 * A new route "managers_dashboard" has been added to expose the
 * ManagersDashboardScreen with a hierarchical agent tree.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiTheme {
                Surface(modifier = Modifier) {
                    AppNavigator()
                }
            }
        }
    }
}

/**
 * Navigation graph for the app.
 */
@Composable
private fun AppNavigator(startDestination: String = Route.Welcome.route) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Welcome.route) {
            WelcomeScreen(
                onNavigateToDashboard = {
                    navController.navigate(Route.ManagersDashboard.route)
                }
            )
        }

        composable(Route.ManagersDashboard.route) {
            ManagersDashboardScreen(
                onAgentClick = { agent ->
                    // For now we just log; extend as needed.
                    // e.g., navController.navigate("agent_detail/${agent.name}")
                }
            )
        }
    }
}

/**
 * Simple sealed class defining the routes used in the NavHost.
 */
private sealed class Route(val route: String) {
    object Welcome : Route("welcome")
    object ManagersDashboard : Route("managers_dashboard")
}


These changes introduce a brand‑new `ManagersDashboardScreen.kt` that visualises a hierarchical agent tree and wire it into the existing navigation flow via an updated `MainActivity.kt`. After committing these files to the `evolution` branch, trigger the usual CI build to verify compilation.