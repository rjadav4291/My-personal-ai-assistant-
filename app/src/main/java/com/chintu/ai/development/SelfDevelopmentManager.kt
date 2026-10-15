package com.chintu.ai.development

import android.util.Base64
import com.chintu.ai.ai.model.AiMessage
import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.MessageRole
import com.chintu.ai.ai.provider.AiProvider

sealed interface DevStatus {
    data object Idle : DevStatus
    data class Planning(val task: String) : DevStatus
    data class PushingToGitHub(val branch: String) : DevStatus
    data class BuildTriggered(val message: String) : DevStatus
    data class Failed(val error: String) : DevStatus
}

class SelfDevelopmentManager(
    private val gitHubService: GitHubService,
    private val aiProvider: AiProvider
) {
    suspend fun handleUserDevelopmentRequest(
        instruction: String,
        onStatus: (DevStatus) -> Unit
    ): String {
        if (!gitHubService.isConfigured()) {
            return "સેલ્ફ-ડેવલપમેન્ટ માટે GitHub PAT Token સેટ કરવું જરૂરી છે. Settings માં જઈને ટોકન ઉમેરો."
        }

        onStatus(DevStatus.Planning(instruction))

        // 1. Generate Branch Name
        val branchName = "feature/auto-" + System.currentTimeMillis().toString().takeLast(6)

        // 2. Fetch Latest Main SHA
        val baseShaResult = gitHubService.getLatestMainSha()
        val baseSha = baseShaResult.getOrElse {
            onStatus(DevStatus.Failed("Main branch SHA fetch failed: ${it.localizedMessage}"))
            return "GitHub સાથે કનેક્ટ થવામાં નિષ્ફળ: ${it.localizedMessage}"
        }

        // 3. Create isolated branch
        val branchCreated = gitHubService.createBranch(branchName, baseSha)
        if (branchCreated.isFailure) {
            return "નવી ડેવલપમેન્ટ બ્રાન્ચ બનાવવામાં ભૂલ આવી."
        }

        onStatus(DevStatus.PushingToGitHub(branchName))

        // 4. Request AI to generate updated code or file
        val aiPrompt = """
            You are CHINTU Self-Development Engine.
            Write a complete Kotlin object or tool implementation based on this user feature request:
            "$instruction"
            Return ONLY raw Kotlin code without markdown formatting or code blocks.
        """.trimIndent()

        val aiResult = aiProvider.execute(
            AiRequest(messages = listOf(AiMessage(role = MessageRole.USER, content = aiPrompt)))
        )

        val generatedCode = aiResult.getOrNull()?.text ?: return "AI કોડ જનરેટ કરી શક્યું નહીં."
        val base64Content = Base64.encodeToString(generatedCode.toByteArray(), Base64.NO_WRAP)

        // 5. Commit to GitHub
        val targetPath = "app/src/main/java/com/chintu/ai/tools/CustomGeneratedTool.kt"
        val commitResult = gitHubService.commitFileChange(
            filePath = targetPath,
            contentBase64 = base64Content,
            commitMessage = "feat: Auto-implemented '$instruction' by CHINTU Engine",
            branch = branchName
        )

        return commitResult.fold(
            onSuccess = {
                onStatus(DevStatus.BuildTriggered("Branch $branchName created & committed."))
                "મેં નવી બ્રાન્ચ ($branchName) બનાવીને કોડ કમિટ કરી દીધો છે! GitHub Actions પર નવું બિલ્ડ આપોઆપ શરૂ થઈ ગયું છે."
            },
            onFailure = {
                onStatus(DevStatus.Failed(it.localizedMessage ?: "Commit failed"))
                "કમિટ કરવામાં ભૂલ આવી: ${it.localizedMessage}"
            }
        )
    }
}
