package com.chintu.ai.agent

import com.chintu.ai.ai.model.AiMessage
import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.MessageRole
import com.chintu.ai.ai.provider.AiProvider
import com.chintu.ai.core.common.AgentState
import com.chintu.ai.core.common.AppMode
import com.chintu.ai.development.SelfDevelopmentManager
import com.chintu.ai.memory.MemoryManager
import com.chintu.ai.skills.SkillManager
import com.chintu.ai.tools.ToolRegistry

class AutonomousConductor(
    private val provider: AiProvider,
    private val memory: MemoryManager,
    private val tools: ToolRegistry,
    private val selfDevManager: SelfDevelopmentManager,
    private val skillManager: SkillManager
) {
    private val dynamicAgentManager = DynamicAgentManager(provider)
    private var pendingFeatureRequest: String? = null

    suspend fun processQuery(
        userInput: String,
        mode: AppMode,
        onStateChange: (AgentState) -> Unit
    ): String {
        val trimmed = userInput.trim()

        if (pendingFeatureRequest != null && isUserConfirmation(trimmed)) {
            val feature = pendingFeatureRequest!!
            pendingFeatureRequest = null
            onStateChange(AgentState.Executing("Files context samji ne repo update kari rahyo chhu..."))

            val upgradeResult = selfDevManager.developAndDeployFeature(
                featureDescription = feature,
                userPrompt = "Add and integrate: $feature"
            )

            return upgradeResult.fold(
                onSuccess = { prUrl ->
                    "Repo ni files samji ne jaruri changes commit thai gaya chhe! 🚀\nGitHub Actions ma navi APK build thava lagi chhe.\n\nBranch: $prUrl"
                },
                onFailure = { error ->
                    "Repo update failed: ${error.localizedMessage}\nGitHub PAT Token check karo."
                }
            )
        }

        if (pendingFeatureRequest != null && isUserRejection(trimmed)) {
            pendingFeatureRequest = null
            return "Samji gayo, upgrade cancel karyu chhe."
        }

        onStateChange(AgentState.Thinking("Task mate specialized sub-agent tayar kari rahyo chhu..."))

        val specializedAgent = dynamicAgentManager.resolveOrCreateAgent(userInput)
        onStateChange(AgentState.Thinking("${specializedAgent.roleName} task handle kari rahyo chhe..."))

        val agentPrompt = """
            Task: $userInput
            Tools:
            ${tools.getAllTools().joinToString("\n") { "- ${it.name}: ${it.description}" }}

            Jo aa task existing tools ke code thi na thai sake, to chelle aa tag lakho:
            [UNSUPPORTED_TASK: feature_name]
        """.trimIndent()

        val agentExecution = dynamicAgentManager.delegateTask(specializedAgent, agentPrompt)
        val replyText = agentExecution.getOrElse { "Error: ${it.localizedMessage}" }

        val marker = "[UNSUPPORTED_TASK:"
        if (replyText.indexOf(marker) >= 0) {
            val featureName = replyText.substringAfter(marker).substringBefore("]").trim()
            pendingFeatureRequest = if (featureName.isNotBlank()) featureName else userInput

            val cleanReply = replyText.replace(Regex("\\[UNSUPPORTED_TASK:.*?\\]"), "").trim()
            return "🤖 **[Assigned Agent: ${specializedAgent.roleName}]**\n$cleanReply\n\n⚠️ Aa task mate repo ni files update karvi pade tem chhe.\nShu hu GitHub repo ni files context samji ne jarur mujab update ke navi file create kari commit kari dau jethi navi APK bani jaay? (ha / na)"
        }

        return "🤖 **[${specializedAgent.roleName}]:**\n$replyText"
    }

    private fun isUserConfirmation(input: String): Boolean {
        val lower = input.lowercase()
        return lower in listOf("હા", "ha", "yes", "y", "kari de", "કરી દે", "બનાવી દે", "ok", "sure", "add it")
    }

    private fun isUserRejection(input: String): Boolean {
        val lower = input.lowercase()
        return lower in listOf("ના", "na", "no", "n", "reva de", "નથી કરવું", "cancel")
    }
}
