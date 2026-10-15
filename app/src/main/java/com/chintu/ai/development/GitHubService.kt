package com.chintu.ai.development

import com.chintu.ai.core.security.SecureStorage
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class GitHubService(
    private val secureStorage: SecureStorage,
    private val owner: String = "rjadav4291",
    private val repo: String = "My-personal-ai-assistant-"
) {
    private val client = HttpClient(Android)

    private fun getToken(): String? = secureStorage.getString("GITHUB_PAT_TOKEN")

    suspend fun isConfigured(): Boolean = !getToken().isNullOrBlank()

    // 1. Get Main Branch Latest Commit SHA
    suspend fun getLatestMainSha(): Result<String> {
        val token = getToken() ?: return Result.failure(IllegalStateException("GitHub Token not found."))
        return try {
            val response = client.get("https://api.github.com/repos/$owner/$repo/git/ref/heads/main") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github.v3+json")
            }
            if (response.status.isSuccess()) {
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val sha = json["object"]?.jsonObject?.get("sha")?.jsonPrimitive?.content ?: ""
                Result.success(sha)
            } else {
                Result.failure(Exception("GitHub API Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Create New Isolated Branch
    suspend fun createBranch(branchName: String, baseSha: String): Result<Boolean> {
        val token = getToken() ?: return Result.failure(IllegalStateException("GitHub Token not found."))
        return try {
            val payload = buildJsonObject {
                put("ref", "refs/heads/$branchName")
                put("sha", baseSha)
            }
            val response = client.post("https://api.github.com/repos/$owner/$repo/git/refs") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github.v3+json")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            if (response.status.isSuccess() || response.status == HttpStatusCode.UnprocessableEntity) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to create branch: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Commit Code Changes directly to Branch
    suspend fun commitFileChange(
        filePath: String,
        contentBase64: String,
        commitMessage: String,
        branch: String
    ): Result<String> {
        val token = getToken() ?: return Result.failure(IllegalStateException("GitHub Token not found."))
        return try {
            val payload = buildJsonObject {
                put("message", commitMessage)
                put("content", contentBase64)
                put("branch", branch)
            }
            val response = client.put("https://api.github.com/repos/$owner/$repo/contents/$filePath") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github.v3+json")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            if (response.status.isSuccess() || response.status == HttpStatusCode.Created) {
                Result.success("Commit successful on branch $branch")
            } else {
                Result.failure(Exception("Commit Error: ${response.status.value} - ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
