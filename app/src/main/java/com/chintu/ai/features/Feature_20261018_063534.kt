package com.chintu.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Simple data model representing a manager.
 */
data class Manager(
    val name: String,
    val role: String
)

/**
 * UI screen that displays a list of managers in a card based list.
 *
 * @param managers The list of managers to be shown.
 * @param onManagerClick Callback when a manager item is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagersDashboardScreen(
    managers: List<Manager>,
    onManagerClick: (Manager) -> Unit = {}
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Managers Dashboard") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(managers) { manager ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onManagerClick(manager) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = manager.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = manager.role,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { /* TODO: edit action */ }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${manager.name}"
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Preview for Android Studio ---------- */

@Preview(showBackground = true)
@Composable
private fun ManagersDashboardScreenPreview() {
    val sampleManagers = listOf(
        Manager(name = "Alice Johnson", role = "Product Manager"),
        Manager(name = "Bob Smith", role = "Engineering Manager"),
        Manager(name = "Catherine Lee", role = "Operations Manager")
    )
    ManagersDashboardScreen(managers = sampleManagers)
}