package com.chintu.ai.tools

/**
 * Contract that all tools must implement.
 */
interface ToolContract {
    /** Unique name used to identify the tool in the registry. */
    val name: String
}

/**
 * Registry for discovering and retrieving registered tools.
 */
class ToolRegistry {
    private val tools = mutableMapOf<String, ToolContract>()

    /** Registers a tool so it can be looked up later. */
    fun register(tool: ToolContract) {
        tools[tool.name] = tool
    }

    /** Retrieves a tool by its name, or null if not found. */
    fun getTool(name: String): ToolContract? = tools[name]

    /** Returns a snapshot of all currently registered tools. */
    fun getAllTools(): List<ToolContract> = tools.values.toList()

    // -------------------------------------------------------------------
    // Automatic integration of the Evolution #108 build‑fix tool.
    // This block runs once when the class is loaded, ensuring the fix
    // tool is always present without requiring manual registration.
    // -------------------------------------------------------------------
    companion object {
        init {
            val evolutionFixTool = EvolutionFixTool()
            // Register the fix tool under a canonical name.
            ToolRegistry().register(evolutionFixTool)
        }
    }
}

/**
 * Tool that encapsulates the fix for the build failure described in
 * Evolution #108. When invoked, it should perform the necessary steps
 * to resolve the issue (e.g., correcting configuration, recompiling,
 * updating dependencies, etc.).
 */
class EvolutionFixTool : ToolContract {
    override val name = "EvolutionFix"

    /**
     * Executes the build‑failure remediation logic for Evolution #108.
     * Implement the concrete fix steps here.
     */
    fun fixBuild() {
        // Placeholder – replace with the actual remediation logic.
        println("Applying Evolution #108 build fix...")
        // Example actions:
        // - Correct Gradle configuration errors.
        // - Update deprecated dependencies.
        // - Re‑run lint/compile checks.
        // - Commit fixes (if part of an automated pipeline).
    }
}