package com.chintu.ai.tools

class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): ToolContract? = tools[name]

    fun getAllTools(): List<ToolContract> = tools.values.toList()
}
