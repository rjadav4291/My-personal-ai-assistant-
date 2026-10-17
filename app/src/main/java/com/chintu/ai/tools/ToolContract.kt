package com.chintu.ai.tools

sealed class ToolResult {
    data class Success(val output: String) : ToolResult()
    data class Error(val message: String) : ToolResult()
    data class Failure(val message: String) : ToolResult()
}

interface ToolContract {
    val name: String
    val description: String
    suspend fun execute(args: Map<String, String>): ToolResult
}
