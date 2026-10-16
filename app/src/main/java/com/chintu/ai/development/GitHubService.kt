package com.chintu.ai.development

import android.util.Base64
import com.chintu.ai.core.security.SecureStorage
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class GitHubService(
    private val secureStorage: SecureStorage
) {
    private val client = HttpClient(Android)
    private val repoOwner = "rjadav4291"
    private val repoName = "My-personal-ai-assistant-"

    private fun getToken(): String {
        return secureStorage.getString("GITHUB_PAT_TOKEN")?.trim() ?: ""
    }

    suspend fun getBranchSha(branch: String = "main"): Result<String> {
        val token = getToken()
        if (token.isBlank()) return Result.failure(IllegalStateException("GITHUB_PAT_TOKEN set karelo nathi."))

        return try {
            val response = client.get("https://api.github.com/repos/$repoOwner/$repoName/git/ref/heads/$branch") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github.v3+json")
            }
            if (response.status.isSuccess()) {
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val sha = json["object"]?.jsonObject?.get("sha")?.jsonPrimitive?.content
                    ?: return Result.failure(Exception("SHA malyo nathi."))
                Result.success(sha)
            } else {
                Result.failure(Exception("Get Branch Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileContent(path: String, branch: String = "main"): Pair<String, String>? {
        val token = getToken()
        if (token.isBlank()) return null
        return try {
            val response = client.get("https://api.github.com/repos/$repoOwner/$repoName/contents/$path?ref=$branch") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github.v3+json")
            }
            if (response.status.isSuccess()) {
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val sha = json["sha"]?.jsonPrimitive?.content ?: ""
                val base64Content = json["content"]?.jsonPrimitive?.content?.replace("\n", "") ?: ""
                val decoded = String(Base64.decode(base64Content, Base64.DEFAULT), Charsets.UTF_8)
                Pair(decoded, sha)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun createBranch(newBranch: String, baseSha: String): Result<Boolean> {
        val token = getToken()
        if (token.isBlank()) return Result.failure(IllegalStateException("GITHUB_PAT_TOKEN set karelo nathi."))

        return try {
            val payload = buildJsonObject {
                put("ref", "refs/heads/$newBranch")
                put("sha", baseSha)
            }
            val response = client.post("https://api.github.com/repos/$repoOwner/$repoName/git/refs") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github.v3+json")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            if (response.status.isSuccess() || response.status.value == 201) {
                Result.success(true)
            } else {
                Result.failure(Exception("Create Branch Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrUpdateFile(
        path: String,
        content: String,
        commitMessage: String,
        branch: String
    ): Result<Boolean> {
        val token = getToken()
        if (token.isBlank()) return Result.failure(IllegalStateException("GITHUB_PAT_TOKEN set karelo nathi."))

        return try {
            val existing = getFileContent(path, branch)
            val encodedContent = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val payload = buildJsonObject {
                put("message", commitMessage)
                put("content", encodedContent)
                put("branch", branch)
                if (existing != null) {
                    put("sha", existing.second)
                }
            }
            val response = client.put("https://api.github.com/repos/$repoOwner/$repoName/contents/$path") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github.v3+json")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            if (response.status.isSuccess() || response.status.value == 201) {
                Result.success(true)
            } else {
                Result.failure(Exception("Update File Error: ${response.status.value} - ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
