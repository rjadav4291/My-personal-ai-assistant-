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
            onStateChange(AgentState.Executing("GitHub પર ફીચર કમિટ કરી રહ્યો છું..."))

            val upgradeResult = selfDevManager.developAndDeployFeature(
                featureDescription = feature,
                userPrompt = "Add new capability: $feature"
            )

            return upgradeResult.fold(
                onSuccess = { prUrl ->
                    "GitHub રેપોઝિટરીમાં '$feature' માટે ફેરફાર સબમિટ થઈ ગયો છે! 🚀\nGitHub Actions માં નવું APK બિલ્ડ શરૂ થઈ ગયું છે.\n\nલિંક: $prUrl"
                },
                onFailure = { error ->
                    "રેપોઝિટરી અપડેટ કરવામાં ભૂલ આવી: ${error.localizedMessage}\nકૃપા કરીને સેટિંગ્સમાં સાચો GitHub PAT Token ચેક કરો."
                }
            )
        }

        if (pendingFeatureRequest != null && isUserRejection(trimmed)) {
            pendingFeatureRequest = null
            return "સમજી ગયો! ફીચર ઉમેરવાનું રદ કર્યું છે."
        }

        onStateChange(AgentState.Thinking("વિચારી રહ્યો છું..."))

        val availableToolsPrompt = tools.getAllTools().joinToString("\n") { "- ${it.name}: ${it.description}" }
        val systemPrompt = """
            તમે CHINTU છો — સ્વાયત્ત AI આસિસ્ટન્ટ.
            તમારી પાસે હાલ આ ટૂલ્સ ઉપલબ્ધ છે:
            $availableToolsPrompt

            નિયમ:
            જો યુઝરનું કાર્ય હાલના ટૂલ્સથી શક્ય ન હોય અને નવું ફીચર ઉમેરવાની જરૂર હોય, તો છેલ્લે આ ફોર્મેટમાં લખો:
            [UNSUPPORTED_TASK: ફીચરનું નામ]
        """.trimIndent()

        val request = AiRequest(
            messages = listOf(AiMessage(role = MessageRole.USER, content = userInput)),
            systemInstruction = systemPrompt
        )

        val responseResult = provider.execute(request)
        val replyText: String = responseResult.fold(
            onSuccess = { it.text },
            onFailure = { "ભૂલ આવી: ${it.localizedMessage}" }
        )

        val marker = "[UNSUPPORTED_TASK:"
        if (replyText.indexOf(marker) >= 0) {
            val featureName = replyText.substringAfter(marker).substringBefore("]").trim()
            pendingFeatureRequest = if (featureName.isNotBlank()) featureName else userInput

            val cleanReply = replyText.replace(Regex("\\[UNSUPPORTED_TASK:.*?\\]"), "").trim()
            return "$cleanReply\n\n⚠️ આ કાર્ય કરવાની ક્ષમતા હાલ ઉપલબ્ધ નથી.\nશું હું આ ફીચરનો કોડ જાતે બનાવીને તમારી GitHub રેપોઝિટરીમાં કમિટ કરી દઉં જેથી નવું APK બની જાય? (હા / ના)"
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
