**

package com.chintu.ai.tools

/**
 * Represents a tool that can execute automated system tasks, workflow routines,
 * and device automation.  This is the core implementation that the rest of the
 * platform will use when the “AutomationAgent” feature is invoked.
 */
class AutomationAgent : ToolContract {
    override val name: String = "AutomationAgent"
    override val description: String =
        "Executes automated system tasks, workflow routines, and device automation."

    /**
     * Executes a requested automation action.
     *
     * Expected argument keys:
     *   - "action": the name of the action to perform (optional)
     *   - "task"  : alias for "action" (optional)
     *
     * If neither is supplied a default routine check is performed.
     *
     * @param args map of argument names to values
     * @return ToolResult indicating success or failure
     */
    override suspend fun execute(args: Map<String, String>): ToolResult {
        val action = args["action"] ?: args["task"] ?: "Default routine check"
        // Here you would plug in the real automation logic.
        // For now we simply acknowledge the request.
        return ToolResult.Success("Automation routine for '$action' initiated successfully.")
    }
}

/**
 * Internal diagnostic tool used for agent evolution and self‑repair.
 */
class EvolutionFixTool : ToolContract {
    override val name: String = "EvolutionFix"
    override val description: String = "Internal agent evolution and self-repair diagnostic tool."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        // Placeholder for actual diagnostic logic.
        return ToolResult.Success("Evolution diagnostics verified.")
    }
}

/**
 * Central registry that holds all available tools.
 *
 * New tools can be added via [registerTool] and are exposed through
 * [getAllTools] and [getTool].
 */
class ToolRegistry {
    private val tools = mutableListOf<ToolContract>()

    init {
        // Register core tools.
        registerTool(AutomationAgent())
        registerTool(EvolutionFixTool())
    }

    /**
     * Adds a tool to the registry if a tool with the same name (case‑insensitive)
     * hasn't already been registered.
     *
     * @param tool the [ToolContract] implementation to register
     */
    fun registerTool(tool: ToolContract) {
        if (tools.none { it.name.equals(tool.name, ignoreCase = true) }) {
            tools.add(tool)
        }
    }

    /** Returns an immutable snapshot of all registered tools. */
    fun getAllTools(): List<ToolContract> = tools.toList()

    /** Retrieves a tool by its name (case‑insensitive) or returns `null` if not found. */
    fun getTool(name: String): ToolContract? =
        tools.find { it.name.equals(name, ignoreCase = true) }
}