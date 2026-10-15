package com.chintu.ai.development

import android.util.Base64
import com.chintu.ai.core.security.SecureStorage
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class GitHubApiClient(private val secureStorage: SecureStorage) {

    private val client = HttpClient(Android)

    private fun getToken(): String? = secureStorage.getString("GITHUB_PAT")
    private fun getRepo(): String? = secureStorage.getString("GITHUB_REPO") // format: "username/repo"

    suspend fun isConfigured(): Boolean {
        return !getToken().isNullOrBlank() && !getRepo().isNullOrBlank()
    }

    suspend fun createBranch(branchName: String): Result<String> {
        val token = getToken() ?: return Result.failure(IllegalStateException("GitHub PAT નથી મળ્યો."))
        val repo = getRepo() ?: return Result.failure(IllegalStateException("GitHub Repo configure નથી."))

        return try {
            // 1. Get SHA of main branch
            val mainRefUrl = "https://api.github.com/repos/$repo/git/ref/heads/main"
            val refResponse = client.get(mainRefUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github.v3+json")
            }

            if (!refResponse.status.isSuccess()) {
                return Result.failure(Exception("Main branch નો SHA મેળવવામાં નિષ્ફળતા: ${refResponse.status.value}"))
            }

            val refJson = Json.parseToJsonElement(refResponse.bodyAsText()).jsonObject
            val sha = refJson["object"]?.jsonObject?.get("sha")?.jsonPrimitive?.content
                ?: return Result.failure(Exception("SHA parsing failed"))

            // 2. Create new reference/branch
            val createRefUrl = "https://api.github.com/repos/$repo/git/refs"
            val payload = buildJsonObject {
                put("ref", "refs/heads/$branchName")
                put("sha", sha)
            }

            val createResponse = client.post(createRefUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github.v3+json")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            if (createResponse.status.isSuccess() || createResponse.status.value == 422) {
                // 422 means branch already exists
                Result.success(branchName)
            } else {
                Result.failure(Exception("Branch બનાવવામાં ભૂલ: ${createResponse.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun commitFile(
        branchName: String,
        filePath: String,
        fileContent: String,
        commitMessage: String
    ): Result<String> {
        val token = getToken() ?: return Result.failure(IllegalStateException("GitHub PAT નથી."))
        val repo = getRepo() ?: return Result.failure(IllegalStateException("GitHub Repo configure નથી."))

        return try {
            // Check if file already exists to get its SHA (required for update)
            val fileUrl = "https://api.github.com/repos/$repo/contents/$filePath?ref=$branchName"
            val existingFileRes = client.get(fileUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github.v3+json")
            }

            var sha: String? = null
            if (existingFileRes.status.isSuccess()) {
                val json = Json.parseToJsonElement(existingFileRes.bodyAsText()).jsonObject
                sha = json["sha"]?.jsonPrimitive?.content
            }

            // Encode content to Base64
            val encodedContent = Base64.encodeToString(fileContent.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

            val commitPayload = buildJsonObject {
                put("message", commitMessage)
                put("content", encodedContent)
                put("branch", branchName)
                sha?.let { put("sha", it) }
            }

            val putResponse = client.put(fileUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github.v3+json")
                contentType(ContentType.Application.Json)
                setBody(commitPayload.toString())
            }

            if (putResponse.status.isSuccess()) {
                Result.success("ફાઇલ સચવાઈ ગઈ અને કમિટ થઈ ગઈ: $filePath")
            } else {
                Result.failure(Exception("Commit નિષ્ફળ ગયું: ${putResponse.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
