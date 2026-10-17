package com.chintu.ai.tools

/**
 * Registry for managing AI tools.
 * Provides registration, retrieval, and evolution capabilities.
 */
class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    /** Register a new tool under its name. */
    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    /**
     * Retrieve a tool by name.
     *
     * @return the tool if present, otherwise null
     */
    fun getTool(name: String): ToolContract? = tools[name]

    /** Return all registered tools as an immutable list. */
    fun getAllTools(): List<ToolContract> = tools.values.toList()

    /**
     * Evolve an existing tool using the provided transformation lambda.
     *
     * @param name      the name of the tool to evolve
     * @param evolution a function that receives the current tool and returns its evolved version
     */
    fun evolveTool(name: String, evolution: (ToolContract) -> ToolContract) {
        val current = tools[name] ?: return
        val evolved = evolution(current)
        tools[name] = evolved
    }
}