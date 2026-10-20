**

package com.chintu.ai.tools

/**
 * Tool that executes basic Git operations. This is a placeholder implementation
 * that simulates git commands and returns a success message. In a real
 * production environment you would integrate with a Git library such as JGit
 * or invoke command‑line git processes with proper error handling and security.
 */
class ExecuteGitOperationsTool : ToolContract {
    override val name: String = "ExecuteGitOperations"
    override val description: String = "Performs git operations like clone, pull, commit, push, etc."

    /**
     * Expected arguments (all optional, but at least one should be provided):
     * - `operation`: The git operation to perform (e.g., clone, pull, push, commit, status).
     * - `repositoryUrl`: URL of the repository (required for clone, push, pull when remote is needed).
     * - `branch`: Branch name (optional, defaults to `master`/`main` depending on operation).
     * - `message`: Commit message (required for commit).
     *
     * The implementation below only simulates the behavior and returns a descriptive
     * success message. Real command execution is omitted for safety.
     */
    override suspend fun execute(args: Map<String, String>): ToolResult {
        val operation = args["operation"]?.lowercase()
            ?: return ToolResult.Failure("Missing required argument: operation")

        // Basic validation of required parameters per operation
        when (operation) {
            "clone" -> {
                val repo = args["repositoryUrl"]
                    ?: return ToolResult.Failure("Missing repositoryUrl for clone operation")
                val branch = args["branch"] ?: "main"
                return ToolResult.Success("Simulated git clone of $repo (branch: $branch) completed.")
            }
            "pull" -> {
                val repo = args["repositoryUrl"] ?: return ToolResult.Failure("Missing repositoryUrl for pull operation")
                val branch = args["branch"] ?: "main"
                return ToolResult.Success("Simulated git pull from $repo (branch: $branch) completed.")
            }
            "push" -> {
                val repo = args["repositoryUrl"] ?: return ToolResult.Failure("Missing repositoryUrl for push operation")
                val branch = args["branch"] ?: "main"
                return ToolResult.Success("Simulated git push to $repo (branch: $branch) completed.")
            }
            "commit" -> {
                val message = args["message"] ?: return ToolResult.Failure("Missing commit message for commit operation")
                return ToolResult.Success("Simulated git commit with message: \"$message\" completed.")
            }
            "status" -> {
                return ToolResult.Success("Simulated git status: Working tree clean.")
            }
            else -> return ToolResult.Failure("Unsupported git operation: $operation")
        }
    }
}

/**
 * Existing automation tool.
 */
class AutomationAgentTool : ToolContract {
    override val name: String = "AutomationAgent"
    override val description: String = "Executes automated system tasks, workflow routines, and device automation."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val action = args["action"] ?: args["task"] ?: "Default routine check"
        return ToolResult.Success("Automation routine for '$action' initiated successfully.")
    }
}

/**
 * Internal evolution/diagnostic tool.
 */
class EvolutionFixTool : ToolContract {
    override val name: String = "EvolutionFix"
    override val description: String = "Internal agent evolution and self-repair diagnostic tool."

    override suspend fun execute(args: Map<String, String>): ToolResult {
        return ToolResult.Success("Evolution diagnostics verified.")
    }
}

/**
 * Registry that holds all available tools.
 */
class ToolRegistry {
    private val tools = mutableListOf<ToolContract>()

    init {
        registerTool(AutomationAgentTool())
        registerTool(EvolutionFixTool())
        // Register the newly added Git operations tool
        registerTool(ExecuteGitOperationsTool())
    }

    fun registerTool(tool: ToolContract) {
        if (tools.none { it.name.equals(tool.name, ignoreCase = true) }) {
            tools.add(tool)
        }
    }

    fun getAllTools(): List<ToolContract> = tools.toList()

    fun getTool(name: String): ToolContract? = tools.find { it.name.equals(name, ignoreCase = true) }
}