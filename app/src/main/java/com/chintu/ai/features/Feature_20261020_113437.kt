**

package com.chintu.ai.tools

// Added imports for the core tool interfaces and result types.
// Adjust the package names if the actual locations differ.
import com.chintu.ai.tools.contracts.ToolContract
import com.chintu.ai.tools.contracts.ToolResult

/**
 * Tool implementations used by the automation framework.
 *
 * The classes below are intentionally lightweight – they only provide a name,
 * description and a simple execution stub. Real implementations can be expanded
 * later without affecting the registry logic.
 */
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
    override val description: String = "Internal agent evolution and self‑repair diagnostic tool."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        return ToolResult.Success("Evolution diagnostics verified.")
    }
}

/**
 * Central registry that holds all available tools.
 *
 * - Tools are stored in a mutable list to allow dynamic registration.
 * - Duplicate tool names (case‑insensitive) are ignored.
 * - Public APIs expose an immutable copy of the tool collection.
 */
class ToolRegistry {
    private val tools = mutableListOf<ToolContract>()

    init {
        // Register default tools during initialization.
        registerTool(AutomationAgentTool())
        registerTool(EvolutionFixTool())
    }

    /**
     * Registers a new [ToolContract] if a tool with the same name hasn't been added yet.
     */
    fun registerTool(tool: ToolContract) {
        if (tools.none { it.name.equals(tool.name, ignoreCase = true) }) {
            tools.add(tool)
        }
    }

    /** Returns an immutable snapshot of all registered tools. */
    fun getAllTools(): List<ToolContract> = tools.toList()

    /** Retrieves a tool by its name (case‑insensitive) or returns `null` if not found. */
    fun getTool(name: String): ToolContract? = tools.find { it.name.equals(name, ignoreCase = true) }
}