package com.chintu.ai.agent

import com.chintu.ai.ai.model.AiMessage
import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.MessageRole
import com.chintu.ai.ai.provider.AiProvider

enum class AgentRole {
    RESEARCHER,
    CODER,
    WRITER
}

class MultiAgentOrchestrator(private val provider: AiProvider) {

    suspend fun executeSubTask(role: AgentRole, taskInput: String): Result<String> {
        val systemInstruction = when (role) {
            AgentRole.RESEARCHER -> "You are CHINTU Research Agent. Analyze information critically, extract verifiable facts, and format clearly."
            AgentRole.CODER -> "You are CHINTU Coding Agent. Write concise, idiomatic, error-handled code with clear explanations."
            AgentRole.WRITER -> "You are CHINTU Writing Agent. Compose articulate, polite, and contextual drafts in Gujarati or English."
        }

        val request = AiRequest(
            messages = listOf(AiMessage(role = MessageRole.USER, content = taskInput)),
            systemInstruction = systemInstruction
        )

        return provider.execute(request).map { it.text }
    }
}
