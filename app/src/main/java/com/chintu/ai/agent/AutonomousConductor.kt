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

        onStateUpdate(AgentState.Thinking("વિચારણા ચાલુ છે..."))

        val trimmed = userInput.trim()

        // 1. Memory Store Intent Check ("યાદ રાખ કે...", "Remember that...")
        if (trimmed.startsWith("યાદ રાખ", ignoreCase = true) || trimmed.startsWith("remember", ignoreCase = true)) {
            val contentToRemember = trimmed
                .removePrefix("યાદ રાખ")
                .removePrefix("remember")
                .removePrefix("કે")
                .removePrefix("that")
                .trim()

            if (contentToRemember.isNotEmpty()) {
                val key = "info_${System.currentTimeMillis()}"
                memory.saveMemory(key, contentToRemember)
                onStateUpdate(AgentState.Ready)
                return "મેં યાદ રાખી લીધું છે: $contentToRemember"
            }
        }

        // 2. Clear Memory Intent Check
        if (trimmed.contains("બધું ભૂલી જા", ignoreCase = true) || trimmed.contains("clear memory", ignoreCase = true)) {
            memory.clearMemories()
            onStateUpdate(AgentState.Ready)
            return "બધી સાચવેલી મેમરી ડિલીટ કરી દેવામાં આવી છે."
        }

        // 3. System Tools Intent Check
        val lower = trimmed.lowercase()
        if (lower.contains("battery") || lower.contains("બેટરી")) {
            onStateUpdate(AgentState.Executing("get_battery_status"))
            val batteryTool = tools.getTool("get_battery_status")
            val toolRes = batteryTool?.execute(emptyMap())
            onStateUpdate(AgentState.Ready)
            return when (toolRes) {
                is ToolResult.Success -> toolRes.message
                is ToolResult.Failure -> "ભૂલ: ${toolRes.cause}"
                null -> "Tool મળ્યું નથી."
            }
        }

        // 4. Autonomous AI Inference with Context Injection
        val savedMemories = memory.getAllMemories().joinToString("\n") { "- ${it.value}" }
        val promptWithContext = if (savedMemories.isNotEmpty()) {
            """
            યુઝર વિશે સાચવેલી વિગતો (Persistent Memory):
            $savedMemories

            યુઝરનો સવાલ/આદેશ:
            $trimmed
            """.trimIndent()
        } else {
            trimmed
        }

        val request = AiRequest(
            messages = listOf(AiMessage(role = MessageRole.USER, content = promptWithContext))
        )

        val result = provider.execute(request)
        onStateUpdate(AgentState.Ready)

        return result.fold(
            onSuccess = { it.text },
            onFailure = { "ભૂલ આવી: ${it.localizedMessage}" }
        )
    }
}
