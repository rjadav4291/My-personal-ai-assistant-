**

package com.chintu.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Direct Upgrade screen – the UI column is vertically scrollable and the **Upgrade**
 * button stays sticky at the bottom of the screen (always visible even when the
 * on‑screen keyboard is shown).
 *
 * This composable can be launched from any navigation host (e.g. NavHost,
 * Activity setContent, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectUpgradeScreen(
    onUpgradeClicked: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    // Optional dialog wrapper – replace with your own navigation if not needed
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Root Column – scrollable content + sticky button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 72.dp) // space for sticky button
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // ==== Example UI fields – replace with actual content ====
                    Text(
                        text = "Direct Upgrade",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Device ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Target Version") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    // Add more fields as required …
                }

                // ==== Sticky Upgrade button ====
                ElevatedButton(
                    onClick = onUpgradeClicked,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(text = "Upgrade")
                }
            }
        }
    }
}

/* --------------------------------------------------------------
   Preview (optional – remove in production if not needed)
   -------------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
private fun DirectUpgradeScreenPreview() {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        DirectUpgradeScreen(
            onUpgradeClicked = { /* TODO: handle upgrade */ },
            onDismiss = { showDialog = false }
        )
    }
}


### Explanation of the implementation
1. **Scrollable Content**  
   - The inner `Column` is wrapped with `Modifier.verticalScroll(rememberScrollState())`.  
   - This lets any number of input fields (or other UI elements) be scrolled when they overflow the screen height.

2. **Sticky Upgrade Button**  
   - The whole UI is placed inside a `Box`.  
   - The button is positioned with `Modifier.align(Alignment.BottomCenter)` so it stays at the bottom of the dialog/screen regardless of scrolling.  
   - A bottom padding (`padding(bottom = 72.dp)`) on the outer `Box` ensures the scrollable content does not get hidden behind the sticky button.

3. **Keyboard‑friendly**  
   - Because the button is **outside** the scrollable column, it remains visible even when the soft keyboard appears.  
   - The layout uses `fillMaxWidth()` and appropriate paddings to keep a clean material design look.

4. **Reusable Component**  
   - The composable receives callbacks (`onUpgradeClicked`, `onDismiss`) making it easy to integrate into the existing navigation flow.

5. **Dialog Wrapper**  
   - A `Dialog` is used for demonstration; replace it with your own navigation container (e.g., a `Scaffold` screen) if needed.

Add this file to your project and invoke `DirectUpgradeScreen` wherever the “Direct Upgrade” UI is required. The button will now stay visible at the bottom, and the rest of the UI will be vertically scrollable.