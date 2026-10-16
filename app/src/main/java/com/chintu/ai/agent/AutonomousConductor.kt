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
            onStateChange(AgentState.Executing("GitHub par upgrade push kari rahyo chhu..."))

            val upgradeResult = selfDevManager.developAndDeployFeature(
                featureDescription = feature,
                userPrompt = "Add new capability: $feature"
            )

            return upgradeResult.fold(
                onSuccess = { prUrl ->
                    "GitHub repo ma '$feature' feature commit thai gayu chhe! 🚀\nGitHub Actions ma navi APK build thava lagi chhe.\n\nPR: $prUrl"
                },
                onFailure = { error ->
                    "Repo update karva ma khami aavi: ${error.localizedMessage}\nGitHub PAT Token barabar chhe ke nahi te check karo."
                }
            )
        }

        if (pendingFeatureRequest != null && isUserRejection(trimmed)) {
            pendingFeatureRequest = null
            return "Samji gayo! Feature add karvanu cancel karyu chhe."
        }

        onStateChange(AgentState.Thinking("Vichari rahyo chhu..."))

        val availableToolsPrompt = tools.getAllTools().joinToString("\n") { "- ${it.name}: ${it.description}" }
        val systemPrompt = """
            Tame CHINTU chho - ek autonomous AI assistant.
            Tamar paase aa tools uplabdh chhe:
            $availableToolsPrompt

            Jo user no task tamara tools thi na thai shake, to saf shabdo ma kaho ane chelle aa format ma lakho:
            [UNSUPPORTED_TASK: feature_name]
        """.trimIndent()

        val request = AiRequest(
            messages = listOf(AiMessage(role = MessageRole.USER, content = userInput)),
            systemInstruction = systemPrompt
        )

        val responseResult = provider.execute(request)
        val replyText: String = responseResult.getOrElse { "Error: ${it.localizedMessage}" }

        val marker = "[UNSUPPORTED_TASK:"
        if (replyText.indexOf(marker) >= 0) {
            val featureName = replyText.substringAfter(marker).substringBefore("]").trim()
            pendingFeatureRequest = if (featureName.isNotBlank()) featureName else userInput

            val cleanReply = replyText.replace(Regex("\\[UNSUPPORTED_TASK:.*?\\]"), "").trim()
            return "$cleanReply\n\n⚠️ Aa feature mara ma nathi.\nShu hu aano code generate kari ne tamari GitHub repo ma commit kari dau jethi navi APK bani jaay? (ha / na ma javab aapo)"
        }

        return replyText
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
