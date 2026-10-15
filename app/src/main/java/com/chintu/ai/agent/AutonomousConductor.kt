package com.chintu.ai.agent

import com.chintu.ai.ai.model.AiMessage
import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.MessageRole
import com.chintu.ai.ai.provider.AiProvider
import com.chintu.ai.core.common.AgentState
import com.chintu.ai.core.common.AppMode
import com.chintu.ai.memory.MemoryManager
import com.chintu.ai.tools.ToolRegistry
import com.chintu.ai.tools.ToolResult

class AutonomousConductor(
    private val provider: AiProvider,
    private val memory: MemoryManager,
    private val tools: ToolRegistry
) {
    suspend fun processQuery(
        userInput: String,
        mode: AppMode,
        onStateUpdate: (AgentState) -> Unit
    ): String {
        if (mode == AppMode.OFFLINE) {
            onStateUpdate(AgentState.Offline)
            return "ઓફલાઇન મોડ ચાલુ છે. નેટવર્ક કનેક્શન વગર વિનંતી પ્રોસેસ કરી શકાશે નહીં."
        }

        onStateUpdate(AgentState.Thinking("પ્રોસેસિંગ..."))

        val lower = userInput.lowercase()
        if (lower.contains("battery") || lower.contains("બેટરી")) {
            onStateUpdate(AgentState.Executing("get_battery_status"))
            val batteryTool = tools.getTool("get_battery_status")
            return when (val res = batteryTool?.execute(emptyMap())) {
                is ToolResult.Success -> res.message
                is ToolResult.Failure -> "ભૂલ: ${res.cause}"
                null -> "Tool મળ્યું નથી."
            }
        }

        val savedMemories = memory.getAllMemories().joinToString("\n") { "- ${it.key}: ${it.value}" }
        val prompt = if (savedMemories.isNotEmpty()) {
            "User persistent memories:\n$savedMemories\n\nUser Question: $userInput"
        } else {
            userInput
        }

        val request = AiRequest(
            messages = listOf(AiMessage(role = MessageRole.USER, content = prompt))
        )

        val result = provider.execute(request)
        onStateUpdate(AgentState.Ready)

        return result.fold(
            onSuccess = { it.text },
            onFailure = { "ભૂલ આવી: ${it.localizedMessage}" }
        )
    }
}
