**

package com.chintu.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface

/**
 * Upgrade screen with scrollable content and a sticky “Push” button at the bottom.
 *
 * - The content area uses `Modifier.verticalScroll` so the whole screen can be scrolled.
 * - `Modifier.imePadding()` ensures the UI is padded when the IME (keyboard) is visible.
 * - The button is placed in the `bottomBar` of a `Scaffold`, making it always visible
 *   (sticky) regardless of the scroll position.
 */
@Composable
fun UpgradeScreen(
    onPushClicked: () -> Unit = {}
) {
    // Scaffold gives us a built‑in slot (`bottomBar`) that stays anchored at the bottom.
    Scaffold(
        bottomBar = {
            // Sticky button container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)          // comfortable padding around the button
                    .imePadding()            // keep it above the keyboard when opened
            ) {
                Button(
                    onClick = onPushClicked,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(text = "Push")
                }
            }
        },
        content = { innerPadding ->
            // Scrollable content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)   // respect Scaffold's inner padding
                    .imePadding()            // also apply IME padding to the scroll area
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),          // general content padding
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---- Sample placeholder content ----
                // Replace these with the actual UI elements of the Upgrade screen.
                Text(
                    text = "Upgrade Options",
                    // styling omitted for brevity – add your own MaterialTheme.typography as needed
                )
                repeat(20) { index ->
                    Text(text = "Item #$index – description goes here.")
                }
                // ------------------------------------
            }
        }
    )
}

/* ------------------- Preview ------------------- */
@Preview(showBackground = true)
@Composable
private fun UpgradeScreenPreview() {
    // Wrap in a Surface to get default Material colors in preview
    Surface(color = Color.White) {
        UpgradeScreen(onPushClicked = { /* preview click */ })
    }
}


### Explanation of the implementation
1. **`Scaffold`** – Provides a `bottomBar` slot that stays fixed at the bottom of the screen, perfect for a sticky button.
2. **`Modifier.imePadding()`** – Applied to both the scrollable `Column` and the button container so that when the keyboard appears, the UI shifts appropriately.
3. **`verticalScroll(rememberScrollState())`** – Makes the main content vertically scrollable.
4. **Sticky button** – Placed inside the `bottomBar` wrapped in a `Box` with `Alignment.Center` to keep it centered horizontally. The `padding(16.dp)` gives it a comfortable tap area and visual separation from screen edges.
5. **Preview** – A `@Preview` composable is included for quick visual verification in Android Studio.

Add this file to the project, and reference `UpgradeScreen` from your navigation graph or activity as required. No changes to the existing `ToolRegistry` are needed for this UI enhancement.