package com.chintu.ai.tools

class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): ToolContract? = tools[name]

    fun getAllTools(): List<ToolContract> = tools.values.toList()

    /**
     * Integrates a new feature/tool into the registry.
     * If the tool is already registered, it will be replaced with the new instance.
     *
     * @param tool The tool implementing [ToolContract] to register or update.
     */
    fun integrateFeature(tool: ToolContract) {
        register(tool)
    }

    /**
     * Retrieves all registered tools that match the specified feature name.
     * Useful when a single feature maps to multiple tools.
     *
     * @param featureName The name of the feature to search for.
     * @return A list of tools associated with the given feature name.
     */
    fun getToolsByFeature(featureName: String): List<ToolContract> {
        return tools.filterKeys { it == featureName }.values.toList()
    }
}