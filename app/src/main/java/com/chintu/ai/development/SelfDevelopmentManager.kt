package com.chintu.ai.development

import com.chintu.ai.ai.model.AiMessage
import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.MessageRole
import com.chintu.ai.ai.provider.AiProvider
import java.text.SimpleDateFormat
import java.util.*

class SelfDevelopmentManager(
    private val gitHubService: GitHubService,
    private val aiProvider: AiProvider
) {
    suspend fun developAndDeployFeature(
        featureDescription: String,
        userPrompt: String
    ): Result<String> {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val branchName = "upgrade-$timeStamp"

            val baseSha = gitHubService.getBranchSha("main").getOrThrow()
            gitHubService.createBranch(branchName, baseSha).getOrThrow()

            val aiResponse = aiProvider.execute(
                AiRequest(
                    messages = listOf(
                        AiMessage(
                            role = MessageRole.USER,
                            content = "Task: $featureDescription. Context: $userPrompt. Provide a clean implementation plan and details."
                        )
                    ),
                    systemInstruction = "You are CHINTU Code Generator. Explain feature evolution cleanly."
                )
            ).getOrThrow()

            val cleanSanitizedText = aiResponse.text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("$", "\\$")

            val filePath = "app/src/main/java/com/chintu/ai/features/Upgrade_${timeStamp}.kt"
            val validKotlinCode = buildString {
                appendLine("package com.chintu.ai.features")
                appendLine()
                appendLine("object Upgrade_${timeStamp} {")
                appendLine("    val timestamp: String = \"$timeStamp\"")
                appendLine("    val feature: String = \"${featureDescription.replace("\"", "\\\"")}\"")
                appendLine("    val notes: String = \"\"\"$cleanSanitizedText\"\"\".trimIndent()")
                appendLine("}")
            }

            gitHubService.createOrUpdateFile(
                path = filePath,
                content = validKotlinCode,
                commitMessage = "feat(autonomous): apply upgrade $featureDescription",
                branch = branchName
            ).getOrThrow()

            Result.success("https://github.com/rjadav4291/My-personal-ai-assistant-/tree/$branchName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
