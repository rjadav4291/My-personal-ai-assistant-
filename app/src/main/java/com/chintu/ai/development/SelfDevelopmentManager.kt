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
            val branchName = "evolution-$timeStamp"

            val baseSha = gitHubService.getBranchSha("main").getOrThrow()
            gitHubService.createBranch(branchName, baseSha).getOrThrow()

            // Step 1: Decision making - Existing file update karvi ke navi file banavvi
            val targetExistingPath = "app/src/main/java/com/chintu/ai/tools/ToolRegistry.kt"
            val existingFile = gitHubService.getFileContent(targetExistingPath, "main")

            val decisionPrompt = """
                Feature request: "$featureDescription"
                Context: "$userPrompt"
                Existing ToolRegistry file code:
                ```kotlin
                ${existingFile?.first ?: "// None"}
                ```

                Decision rule:
                - Jo aa feature existing system/registry ma update thavu joiye to decision "UPDATE" aapo.
                - Jo aano alag component/feature file banavvi joiye to "NEW_FILE" aapo.
                Output format:
                DECISION: UPDATE or NEW_FILE
                FILE_PATH: target relative file path
                CODE:
                <complete Kotlin code>
            """.trimIndent()

            val aiResponse = aiProvider.execute(
                AiRequest(
                    messages = listOf(AiMessage(role = MessageRole.USER, content = decisionPrompt)),
                    systemInstruction = "You are an autonomous Android system architect. Write pure valid code."
                )
            ).getOrThrow()

            val rawOutput = aiResponse.text
            val isUpdate = rawOutput.contains("DECISION: UPDATE")
            val targetPath = if (isUpdate) {
                targetExistingPath
            } else {
                "app/src/main/java/com/chintu/ai/features/Feature_${timeStamp}.kt"
            }

            val extractedCode = if (rawOutput.contains("CODE:")) {
                rawOutput.substringAfter("CODE:").replace("```kotlin", "").replace("```", "").trim()
            } else {
                rawOutput.replace("```kotlin", "").replace("```", "").trim()
            }

            // Step 2: In-place update or new file creation on GitHub
            gitHubService.createOrUpdateFile(
                path = targetPath,
                content = extractedCode,
                commitMessage = if (isUpdate) "feat(update): in-place update for $featureDescription" else "feat(new): create module for $featureDescription",
                branch = branchName
            ).getOrThrow()

            // Step 3: Evolution audit record
            val auditLogPath = "app/src/main/java/com/chintu/ai/features/Upgrade_${timeStamp}.kt"
            val auditContent = """
                package com.chintu.ai.features

                object Upgrade_${timeStamp} {
                    const val FEATURE = "${featureDescription.replace("\"", "\\\"")}"
                    const val TARGET_FILE = "$targetPath"
                    const val ACTION = "${if (isUpdate) "UPDATED" else "CREATED"}"
                    const val TIMESTAMP = "$timeStamp"
                }
            """.trimIndent()

            gitHubService.createOrUpdateFile(
                path = auditLogPath,
                content = auditContent,
                commitMessage = "docs: autonomous audit record for $featureDescription",
                branch = branchName
            ).getOrThrow()

            Result.success("https://github.com/rjadav4291/My-personal-ai-assistant-/tree/$branchName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
