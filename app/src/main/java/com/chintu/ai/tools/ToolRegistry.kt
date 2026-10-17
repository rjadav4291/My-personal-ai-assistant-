package com.chintu.ai.tools

/**
 * New tool for the requested feature: feature_name
 *
 * This tool follows the same contract as the existing tools and is registered
 * in the [ToolRegistry] during initialization.
 */
class FeatureNameTool : ToolContract {
    override val name: String = "FeatureName"
    override val description: String = "Executes the 'feature_name' functionality as requested."

    /**
     * Executes the feature logic.
     *
     * Expected arguments (all optional):
     * - `operation` – a string describing the specific operation to perform.
     * - any additional key‑value pairs required by the feature.
     *
     * @return a [ToolResult] indicating success or failure.
     */
    override suspend fun execute(args: Map<String, String>): ToolResult {
        // Extract the primary operation argument; fall back to a default action.
        val operation = args["operation"] ?: "default"
        // Here you would place the actual implementation of the feature.
        // For now we return a success message indicating the operation was received.
        return ToolResult.Success("Feature 'feature_name' executed with operation: '$operation'.")
    }
}

/**
 * Central registry for all available tools.
 *
 * Existing tools are registered in the initializer; the new [FeatureNameTool] is also
 * added here so it can be discovered via `getTool` or `getAllTools`.
 */
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