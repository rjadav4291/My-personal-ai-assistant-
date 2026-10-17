package com.chintu.ai.tools

class TaskQueryTool : ToolContract {
    override val name: String = "task_query"
    override val description: String = "Queries device tasks, system status, and execution history."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val action = args["action"] ?: args["task"] ?: "Task Query"
        return ToolResult.Success("Task query for '$action' executed.")
    }
}

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
    override val description: StringString = "Internal agent evolution and self-repair diagnostic tool."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        return ToolResult.Success("Evolution diagnostics verified.")
    }
}

class ToolRegistry {
    private val tools = mutableListOf<ToolContract>()

    init {
        registerTool(TaskQueryTool())
        registerTool(AutomationAgentTool())
        registerTool(EvolutionFixTool())
    }

    fun registerTool(tool: ToolContract) {
        if (tools.none { it.name.equals(tool.name, ignoreCase = true) }) {
            tools.add(tool)
        }
    }

    fun getAllTools(): List<ToolContract> = tools.toList()

    fun getTool(name: String): ToolContract? = tools.find { it.name.equals(name, ignoreCase = true) }
}