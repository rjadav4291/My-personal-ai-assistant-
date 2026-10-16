package com.chintu.ai.agent

import com.chintu.ai.ai.model.AiMessage
import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.MessageRole
import com.chintu.ai.ai.provider.AiProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SubAgent(
    val id: String,
    val roleName: String,
    val expertise: String,
    val systemPrompt: String
)

class DynamicAgentManager(
    private val aiProvider: AiProvider
) {
    private val activeAgents = mutableMapOf<String, SubAgent>()

    suspend fun resolveOrCreateAgent(userTask: String): SubAgent {
        val analyzePrompt = """
            Task: "$userTask"
            Analyze this task and identify or design the specialized sub-agent needed.
            Output strict JSON only:
            {
              "id": "agent_id",
              "roleName": "Role Name",
              "expertise": "Expertise Description",
              "systemPrompt": "System instruction prompt for this agent"
            }
        """.trimIndent()

        val response = aiProvider.execute(
            AiRequest(
                messages = listOf(AiMessage(role = MessageRole.USER, content = analyzePrompt)),
                systemInstruction = "Output valid JSON only without markdown code blocks."
            )
        ).getOrNull()?.text ?: ""

        val cleanJson = response.replace("```json", "").replace("```", "").trim()

        return try {
            val agent = Json.decodeFromString<SubAgent>(cleanJson)
            activeAgents[agent.id] = agent
            agent
        } catch (_: Exception) {
            SubAgent(
                id = "general_agent",
                roleName = "Specialist Agent",
                expertise = "Task Analysis & Resolution",
                systemPrompt = "You are a specialized agent executing user tasks."
            )
        }
    }

    suspend fun delegateTask(agent: SubAgent, taskDetails: String): Result<String> {
        return aiProvider.execute(
            AiRequest(
                messages = listOf(AiMessage(role = MessageRole.USER, content = taskDetails)),
                systemInstruction = agent.systemPrompt
            )
        ).map { it.text }
    }
}
