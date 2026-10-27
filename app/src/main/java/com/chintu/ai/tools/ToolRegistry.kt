package com.chintu.ai.tools

/**
 * Local contract for tools used by the AI assistant.
 *
 * Each tool must provide a name, a description and an executable function.
 * The default implementation of [exec] returns an empty string, allowing
 * concrete tools to override it only when needed.
 */
interface ToolContract {
    val name: String
    val description: String

    /**
     * Executes the tool with a raw string argument.
     *
     * @param args The arguments as a JSON‑encoded string or any other format the tool expects.
     * @return The result of the execution as a string.
     */
    suspend fun exec(args: String): String = ""
}

/**
 * Example tool that executes automated system tasks, workflow routines, and device automation.
 */
class AutomationAgentTool : ToolContract {
    override val name: String = "AutomationAgent"
    override val description: String = "Executes automated system tasks, workflow routines, and device automation."

    /**
     * Executes the automation routine.
     *
     * The original implementation used a Map‑based signature; it is now adapted to the new
     * [ToolContract.exec] contract. The arguments are expected to be a JSON string with an
     * `"action"` (or `"task"`) field. For simplicity, we parse it manually.
     */
    override suspend fun exec(args: String): String {
        // Simple extraction without a full JSON parser – suitable for quick prototypes.
        val action = args
            .removeSurrounding("{", "}")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .associate {
                val (k, v) = it.split(":")
                k.trim() to v.trim()
            }["action"] ?: args // fallback to raw args if key not found

        return "Automation routine for '$action' initiated successfully."
    }
}

/**
 * Internal agent evolution and self‑repair diagnostic tool.
 */
class EvolutionFixTool : ToolContract {
    override val name: String = "EvolutionFix"
    override val description: String = "Internal agent evolution and self-repair diagnostic tool."

    override suspend fun exec(args: String): String {
        // No arguments are needed for this tool; ignore the input.
        return "Evolution diagnostics verified."
    }
}

/**
 * Registry that holds all available tools.
 *
 * The registry now works with the updated [ToolContract] interface.
 */
class ToolRegistry {
    private val tools = mutableListOf<ToolContract>()

    init {
        // Register built‑in tools.
        registerTool(AutomationAgentTool())
        registerTool(EvolutionFixTool())
    }

    /**
     * Registers a new tool if a tool with the same name (case‑insensitive) does not already exist.
     *
     * @param tool The tool to register.
     */
    fun registerTool(tool: ToolContract) {
        if (tools.none { it.name.equals(tool.name, ignoreCase = true) }) {
            tools.add(tool)
        }
    }

    /**
     * Returns an immutable list of all registered tools.
     */
    fun getAllTools(): List<ToolContract> = tools.toList()

    /**
     * Retrieves a tool by its name (case‑insensitive).
     *
     * @param name The name of the tool to retrieve.
     * @return The matching [ToolContract] or `null` if not found.
     */
    fun getTool(name: String): ToolContract? =
        tools.find { it.name.equals(name, ignoreCase = true) }
}