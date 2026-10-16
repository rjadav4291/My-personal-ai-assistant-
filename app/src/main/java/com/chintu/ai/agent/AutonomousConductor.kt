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
    // છેલ્લે અપૂર્ણ રહેલા ટાસ્કને યાદ રાખવા માટેનો વેરિયેબલ
    private var pendingFeatureRequest: String? = null

    suspend fun processQuery(
        userInput: String,
        mode: AppMode,
        onStateChange: (AgentState) -> Unit
    ): String {
        val trimmed = userInput.trim()

        // ૧. જો યુઝરે સેલ્ફ-અપગ્રેડ માટે મંજૂરી (હા / Yes) આપી હોય
        if (pendingFeatureRequest != null && isUserConfirmation(trimmed)) {
            val feature = pendingFeatureRequest!!
            pendingFeatureRequest = null
            onStateChange(AgentState.Executing)

            val upgradeResult = selfDevManager.developAndDeployFeature(
                featureDescription = feature,
                userPrompt = "Add new capability: $feature"
            )

            return upgradeResult.fold(
                onSuccess = { prUrl ->
                    "મેં મારી જાતે '$feature' ફીચર તમારી GitHub રેપોઝિટરીમાં કમિટ કરી દીધું છે! 🚀\nGitHub Actions માં નવું APK બિલ્ડ થવાનું શરૂ થઈ ગયું છે. થોડી વારમાં Actions ટેબમાંથી નવું APK ડાઉનલોડ કરી શકશો.\n\nલિંક: $prUrl"
                },
                onFailure = { error ->
                    "માફ કરશો, રેપોઝિટરી અપડેટ કરવામાં ખામી આવી: ${error.localizedMessage}\nકૃપા કરીને સેટિંગ્સમાં GitHub PAT Token બરાબર તપાસો."
                }
            )
        }

        // જો યુઝરે ના પાડી હોય
        if (pendingFeatureRequest != null && isUserRejection(trimmed)) {
            pendingFeatureRequest = null
            return "સમજી ગયો! મેં આ ફીચર ઉમેરવાનું રદ કર્યું છે. બીજું શું કામ છે?"
        }

        // ૨. સામાન્ય ક્વેરી પ્રોસેસિંગ
        onStateChange(AgentState.Thinking("વિચારી રહ્યો છું..."))

        val availableToolsPrompt = tools.getAllTools().joinToString("\n") { "- ${it.name}: ${it.description}" }
        val systemPrompt = """
            તમે CHINTU છો — એક સ્વાયત્ત અને શક્તિશાળી AI આસિસ્ટન્ટ.
            તમારી પાસે હાલ આ ટૂલ્સ ઉપલબ્ધ છે:
            $availableToolsPrompt

            નિયમ:
            જો યુઝરનું કાર્ય હાલના ટૂલ્સ કે સામાન્ય માહિતીથી ન થઈ શકે તેવું એડવાન્સ ફીચર હોય (જેમ કે નવી API, નવું સિસ્ટમ ટૂલ, સ્પેશિયલ કેલ્ક્યુલેશન), તો સ્પષ્ટ જણાવો કે આ ફીચર હાલ મારામાં નથી.
            અને અંતે બરાબર આ ફોર્મેટમાં લખો:
            [UNSUPPORTED_TASK: ફીચરનું ટૂંકું નામ]
        """.trimIndent()

        val request = AiRequest(
            messages = listOf(AiMessage(role = MessageRole.USER, content = userInput)),
            systemInstruction = systemPrompt
        )

        val responseResult = provider.execute(request)
        val replyText = responseResult.getOrElse { "ભૂલ આવી: ${it.localizedMessage}" }

        // ૩. ટાસ્ક અનસપોર્ટેડ છે કે નહીં તે ચકાસવું
        if (replyText.contains("[UNSUPPORTED_TASK:")) {
            val featureName = replyText.substringAfter("[UNSUPPORTED_TASK:").substringBefore("]").trim()
            pendingFeatureRequest = if (featureName.isNotBlank()) featureName else userInput

            val cleanReply = replyText.replace(Regex("\\[UNSUPPORTED_TASK:.*?\\]"), "").trim()
            return "$cleanReply\n\n⚠️ આ કાર્ય કરવા માટેની ક્ષમતા હાલ મારી સિસ્ટમમાં ઉપલબ્ધ નથી.\nશું હું આ ફીચરનો કોડ જાતે બનાવીને તમારી GitHub રેપોઝિટરીમાં કમિટ કરી દઉં જેથી નવું APK બની જાય? (હા / ના માં જવાબ આપો)"
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
