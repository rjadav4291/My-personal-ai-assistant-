**

package com.chintu.ai.tools

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ---------------------------------------------------------------------------
 *  Universal Android App Automation Engine
 * ---------------------------------------------------------------------------
 *
 * This file contains:
 * 1) Updated AutomationAgentTool – now capable of:
 *      • Listing installed apps
 *      • Launching an app by its package name
 *      • Performing UI actions (click, type, scroll) via the AccessibilityService
 *
 * 2) AutomationAccessibilityService – a thin AccessibilityService that receives
 *    commands from AutomationAgentTool and executes them on the foreground UI.
 *
 * NOTE:
 * • Add the following to AndroidManifest.xml:
 *      <service
 *          android:name=".tools.AutomationAccessibilityService"
 *          android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
 *          android:exported="false">
 *          <intent-filter>
 *              <action android:name="android.accessibilityservice.AccessibilityService" />
 *          </intent-filter>
 *          <meta-data
 *              android:name="android.accessibilityservice"
 *              android:resource="@xml/automation_accessibility_service_config" />
 *      </service>
 *
 * • Create res/xml/automation_accessibility_service_config.xml with the usual
 *   AccessibilityService configuration (can be minimal for testing).
 *
 * • Request the following runtime permissions where needed:
 *      – PACKAGE_USAGE_STATS (if you want usage stats)
 *      – READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE (optional for file ops)
 *
 * ---------------------------------------------------------------------------
 */

private const val TAG = "AutomationEngine"

/**
 * Data class representing a simple view of an installed app.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long
)

/**
 * AccessibilityService that executes UI actions on behalf of AutomationAgentTool.
 * The service is kept as a singleton (via companion object) so the tool can
 * safely post commands to it from any thread.
 */
class AutomationAccessibilityService : AccessibilityService() {

    companion object {
        /** Holds the currently running service instance. */
        @Volatile
        var instance: AutomationAccessibilityService? = null
            private set

        /** Queue of pending UI actions. */
        private val pendingActions = mutableListOf<suspend () -> Unit>()

        /**
         * Enqueue an action to be performed on the UI thread.
         * The service will execute actions sequentially.
         */
        fun postAction(action: suspend () -> Unit) {
            synchronized(pendingActions) {
                pendingActions.add(action)
            }
            instance?.runPendingActions()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "AutomationAccessibilityService connected")
    }

    override fun onInterrupt() {
        Log.w(TAG, "AutomationAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.i(TAG, "AutomationAccessibilityService destroyed")
    }

    /**
     * Executes pending actions on the main (UI) thread.
     */
    private fun runPendingActions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val actionsCopy: List<suspend () -> Unit>
            synchronized(pendingActions) {
                actionsCopy = pendingActions.toList()
                pendingActions.clear()
            }
            actionsCopy.forEach { action ->
                // launch a coroutine on the UI thread (Dispatchers.Main)
                // but we are already on main, so we can just run it.
                try {
                    // Since the lambda is suspend, we need a coroutine scope.
                    // Use a simple runBlocking for brevity.
                    kotlinx.coroutines.runBlocking {
                        action()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error executing UI action", e)
                }
            }
        }
    }

    /** ------------------- UI Helper Functions ------------------- */

    /** Finds the first node that matches the given text (exact match). */
    private fun findNodeByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (root == null) return null
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.text != null && node.text.toString() == text) {
                return node
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    /** Performs a click on the supplied node if possible. */
    private fun clickNode(node: AccessibilityNodeInfo?): Boolean {
        var target = node
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        return target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }

    /** Types the given text into the currently focused input field. */
    private fun typeText(text: String): Boolean {
        val focused = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_SET_TEXT, text) }
        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    /** Scrolls forward (down) in a scrollable view. */
    private fun scrollForward(): Boolean {
        val scrollable = rootInActiveWindow?.let { findScrollable(it) } ?: return false
        return scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    /** Helper to locate a scrollable node in the hierarchy. */
    private fun findScrollable(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.isScrollable) return node
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    // --------------------------------------------------------------------
}

/**
 * AutomationAgentTool – now a thin wrapper around the engine.
 *
 * Supported actions (passed via args map):
 *   - "list_apps" : returns JSON array of installed apps.
 *   - "launch_app" : requires "package" argument.
 *   - "click"      : requires "text" (the button/label to click).
 *   - "type"       : requires "text" (the text to type into focused field).
 *   - "scroll"     : no extra args, performs a forward scroll.
 *
 * Any unknown action returns a helpful message.
 */
class AutomationAgentTool(private val appContext: Context) : ToolContract {

    override val name: String = "AutomationAgent"
    override val description: String =
        "Executes automated system tasks: list/launch apps and UI interactions via AccessibilityService."

    override suspend fun execute(args: Map<String, String>): ToolResult = withContext(Dispatchers.IO) {
        val action = args["action"] ?: args["task"] ?: "list_apps"
        try {
            when (action) {
                "list_apps" -> {
                    val apps = listInstalledApps()
                    ToolResult.Success(apps.joinToString(separator = "\n") { "${it.appName} (${it.packageName})" })
                }
                "launch_app" -> {
                    val pkg = args["package"]
                        ?: return@withContext ToolResult.Failure("Missing 'package' argument for launch_app.")
                    launchApp(pkg)
                    ToolResult.Success("Launch intent sent for package: $pkg")
                }
                "click" -> {
                    val text = args["text"]
                        ?: return@withContext ToolResult.Failure("Missing 'text' argument for click.")
                    AutomationAccessibilityService.postAction {
                        val node = AutomationAccessibilityService.instance?.rootInActiveWindow?.let {
                            AutomationAccessibilityService.instance?.findNodeByText(it, text)
                        }
                        val clicked = AutomationAccessibilityService.instance?.clickNode(node) ?: false
                        Log.i(TAG, "Clicked node with text '$text': $clicked")
                    }
                    ToolResult.Success("Click command queued for UI element with text: $text")
                }
                "type" -> {
                    val text = args["text"]
                        ?: return@withContext ToolResult.Failure("Missing 'text' argument for type.")
                    AutomationAccessibilityService.postAction {
                        val typed = AutomationAccessibilityService.instance?.typeText(text) ?: false
                        Log.i(TAG, "Typed text '$text': $typed")
                    }
                    ToolResult.Success("Type command queued for text: $text")
                }
                "scroll" -> {
                    AutomationAccessibilityService.postAction {
                        val scrolled = AutomationAccessibilityService.instance?.scrollForward() ?: false
                        Log.i(TAG, "Scroll forward performed: $scrolled")
                    }
                    ToolResult.Success("Scroll command queued.")
                }
                else -> ToolResult.Failure("Unsupported action '$action'. Supported: list_apps, launch_app, click, type, scroll.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "AutomationAgentTool execution error", e)
            ToolResult.Failure("Automation error: ${e.localizedMessage}")
        }
    }

    /** ------------------- Helper Functions ------------------- */

    private fun listInstalledApps(): List<AppInfo> {
        val pm = appContext.packageManager
        val packages: List<PackageInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }

        return packages.map { pkgInfo ->
            val appName = pm.getApplicationLabel(pkgInfo.applicationInfo).toString()
            val versionName = pkgInfo.versionName ?: "N/A"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }
            AppInfo(
                packageName = pkgInfo.packageName,
                appName = appName,
                versionName = versionName,
                versionCode = versionCode
            )
        }.sortedBy { it.appName.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)
            ?: throw IllegalArgumentException("No launch intent for package $packageName")
        launch