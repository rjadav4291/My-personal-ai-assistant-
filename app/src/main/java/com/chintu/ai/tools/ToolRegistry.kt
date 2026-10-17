package com.chintu.ai.tools

class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    init {
        register(MeansNowTryTool())
    }

    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): ToolContract? = tools[name]

    fun getAllTools(): List<ToolContract> = tools.values.toList()
}

private class MeansNowTryTool : ToolContract {
    override val name = "means_now_try"
    override val description = "Executes the means_now_try operation for testing and validation purposes"
    override val parameters = mapOf(
        "input" to ParameterSchema(
            type = "string",
            description = "Input data to process",
            required = true
        ),
        "mode" to ParameterSchema(
            type = "string",
            description = "Execution mode: 'test', 'validate', or 'execute'",
            required = false,
            default = "test"
        )
    )

    override fun execute(args: Map<String, Any>): ToolResult {
        val input = args["input"] as? String ?: return ToolResult.error("Missing required parameter: input")
        val mode = args["mode"] as? String ?: "test"

        return when (mode) {
            "test" -> ToolResult.success("Test mode: Processed '$input' successfully")
            "validate" -> ToolResult.success("Validation mode: '$input' is valid")
            "execute" -> ToolResult.success("Execution mode: Completed processing '$input'")
            else -> ToolResult.error("Unknown mode: $mode. Use 'test', 'validate', or 'execute'")
        }
    }
}