**

package com.chintu.ai.tools

// Existing tools --------------------------------------------------------------

class AutomationAgentTool : ToolContract {
    override val name: String = "AutomationAgent"
    override val description: String = "Executes automated system tasks, workflow routines, and device automation."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val action = args["action"] ?: args["task"] ?: "Default routine check"
        return ToolResult.Success("Automation routine for '$action' initiated successfully.")
    }
}

class EvolutionFixTool : ToolContract {
    override val name: String = "EvolutionFix"
    override val description: String = "Internal agent evolution and self-repair diagnostic tool."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        return ToolResult.Success("Evolution diagnostics verified.")
    }
}

// -----------------------------------------------------------------------------
// New feature tool ------------------------------------------------------------

/**
 * Tool for the requested **feature_name** feature.
 *
 * This tool can be expanded with feature‑specific logic.
 */
class FeatureNameTool : ToolContract {
    override val name: String = "FeatureName"
    override val description: String = "Executes the FeatureName functionality as requested."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        // Example placeholder implementation – replace with real logic as needed.
        val param = args["param"] ?: "default"
        return ToolResult.Success("FeatureName executed with param = '$param'.")
    }
}

// -----------------------------------------------------------------------------
// Tool registry --------------------------------------------------------------

class ToolRegistry {
    private val tools = mutableListOf<ToolContract>()

    init {
        registerTool(AutomationAgentTool())
        registerTool(EvolutionFixTool())
        // Register the newly added feature tool
        registerTool(FeatureNameTool())
    }

    fun registerTool(tool: ToolContract) {
        if (tools.none { it.name.equals(tool.name, ignoreCase = true) }) {
            tools.add(tool)
        }
    }

    fun getAllTools(): List<ToolContract> = tools.toList()

    fun getTool(name: String): ToolContract? = tools.find { it.name.equals(name, ignoreCase = true) }
}