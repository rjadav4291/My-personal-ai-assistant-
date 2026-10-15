package com.chintu.ai.tools

import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class AndroidSystemTools(private val context: Context) {

    fun getBatteryTool(): ToolContract = object : ToolContract {
        override val name: String = "get_battery_status"
        override val description: String = "Returns the current battery level and charging state"

        override suspend fun execute(args: Map<String, String>): ToolResult {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                ?: return ToolResult.Failure("BatteryManager unavailable")
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = bm.isCharging
            return ToolResult.Success("Battery is at $level% (Charging: $isCharging)")
        }
    }

    fun getLaunchAppTool(): ToolContract = object : ToolContract {
        override val name: String = "launch_application"
        override val description: String = "Launches an installed app. Argument 'package_name' required."

        override suspend fun execute(args: Map<String, String>): ToolResult {
            val pkg = args["package_name"] ?: return ToolResult.Failure("Missing argument 'package_name'")
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                ?: return ToolResult.Failure("App with package $pkg not found")
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return ToolResult.Success("Launched application: $pkg")
        }
    }
}
