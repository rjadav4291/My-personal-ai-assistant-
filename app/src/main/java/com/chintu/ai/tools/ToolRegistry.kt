package com.chintu.ai.tools

/** Registry that holds all available tools. */
class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    // Register the new automation agent tool when the registry is instantiated.
    init {
        register(AutomationAgentTool())
    }

    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): ToolContract? = tools[name]

    fun getAllTools(): List<ToolContract> = tools.values.toList()
}

/** Tool contract that each tool must implement. */
interface ToolContract {
    val name: String
    /** Executes the tool with the given parameters and returns a result map. */
    fun execute(params: Map<String, Any>): Map<String, Any>
}

/**
 * AutomationAgentTool – an agent that gathers required information from the user
 * and then sets up the requested automation.
 *
 * In a real implementation the `execute` method would interact with a UI or
 * conversational flow to collect missing data, then invoke the automation
 * creation APIs. For the purpose of this example we simply acknowledge the
 * request and echo back the received parameters.
 */
class AutomationAgentTool : ToolContract {
    override val name: String = "automation_agent"

    override fun execute(params: Map<String, Any>): Map<String, Any> {
        // TODO: Implement actual information‑gathering and automation‑setup logic.
        // For now we just return a confirmation.
        return mapOf(
            "status" to "Automation agent triggered – please provide required details.",
            "receivedParams" to params
        )
    }
}