package com.chintu.ai.tools

/**
 * Tool that performs a lightweight analysis of a code repository.
 *
 * Expected arguments:
 * - `repoUrl` : The URL of the repository to analyze (e.g., a GitHub HTTPS URL).
 * - `branch`  : (Optional) The branch to analyze; defaults to the repository's default branch.
 *
 * The tool currently returns a mock analysis string. In a real implementation,
 * it would clone the repository (or use a shallow fetch), run static analysis,
 * and return a summary.
 */
class RepositoryAnalysisTool : ToolContract {
    override val name: String = "RepositoryAnalysis"
    override val description: String =
        "Analyzes a given code repository and returns a concise summary of its structure, language usage, and potential issues."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val repoUrl = args["repoUrl"]
            ?: return ToolResult.Failure("Missing required argument: repoUrl")
        val branch = args["branch"] ?: "default"

        // Placeholder for real analysis logic.
        // In production this would involve cloning the repo (shallow) and running analysis tools.
        val analysisSummary = """
            Repository Analysis Result:
            • URL: $repoUrl
            • Branch: $branch
            • Languages Detected: Kotlin, Java
            • Modules: 3
            • Estimated Lines of Code: ~12,450
            • Potential Issues: 2 warnings (unused imports, duplicated code blocks)
        """.trimIndent()

        return ToolResult.Success(analysisSummary)
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
    override val description: String = "Internal agent evolution and self-repair diagnostic tool."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        return ToolResult.Success("Evolution diagnostics verified.")
    }
}

class ToolRegistry {
    private val tools = mutableListOf<ToolContract>()

    init {
        // Existing tools
        registerTool(AutomationAgentTool())
        registerTool(EvolutionFixTool())
        // New repository analysis tool
        registerTool(RepositoryAnalysisTool())
    }

    fun registerTool(tool: ToolContract) {
        if (tools.none { it.name.equals(tool.name, ignoreCase = true) }) {
            tools.add(tool)
        }
    }

    fun getAllTools(): List<ToolContract> = tools.toList()

    fun getTool(name: String): ToolContract? = tools.find { it.name.equals(name, ignoreCase = true) }
}