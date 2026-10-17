package com.chintu.ai.tools

// Tool contract interface – defines the shape of any registered tool.
interface ToolContract {
    val name: String
    fun execute(input: String): String
}

/**
 * Registry that holds instances of [ToolContract] keyed by their names.
 */
class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    /** Register a new tool – overwrites any existing tool with the same name. */
    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    /** Retrieve a tool by its name, or `null` if it is not registered. */
    fun getTool(name: String): ToolContract? = tools[name]

    /** Return a snapshot of all currently registered tools. */
    fun getAllTools(): List<ToolContract> = tools.values.toList()

    /**
     * Companion utilities for integrating default tools.
     * Call `ToolRegistry.registerImageGenerationTool(myRegistry)` during
     * application startup to make the image‑generation capability available.
     */
    companion object {
        fun registerImageGenerationTool(registry: ToolRegistry) {
            registry.register(ImageGenerationTool())
        }
    }
}

/**
 * Concrete implementation of an image‑generation tool.
 * In a production scenario this would invoke an external AI service
 * (e.g., OpenAI DALL‑E, Stable Diffusion) and return the generated image URL
 * or binary data. The current placeholder demonstrates the contract.
 */
class ImageGenerationTool : ToolContract {
    override val name = "image_generation"

    override fun execute(prompt: String): String {
        // Placeholder – real implementation would call an image‑generation API.
        return "generated_image_for_$prompt.png"
    }
}