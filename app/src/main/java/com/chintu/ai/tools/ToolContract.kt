package com.chintu.ai.tools

sealed interface ToolResult {
    data class Success(val message: String) : ToolResult
    data class Failure(val cause: String) : ToolResult
}

interface ToolContract {
    val name: String
    val description: String
    suspend fun execute(args: Map<String, String>): ToolResult
}
