package com.chintu.ai.core.common

enum class AppMode {
    AUTO,
    ONLINE,
    OFFLINE,
    PRIVATE
}

sealed interface AgentState {
    data object Ready : AgentState
    data object Listening : AgentState
    data class Thinking(val task: String) : AgentState
    data class Executing(val toolName: String) : AgentState
    data class Speaking(val text: String) : AgentState
    data class Error(val message: String) : AgentState
    data object Offline : AgentState
}
