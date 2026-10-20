package com.chintu.ai.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.IOException
import java.util.Base64
import kotlin.text.Charsets.UTF_8

/**
 * Tool that commits and pushes files to a GitHub repository using a personal access token.
 *
 * Expected arguments:
 *   - repo          : full repository name in the form "owner/repo" (e.g. "rjadav4291/My-personal-ai-assistant-")
 *   - branch        : target branch (e.g. "main")
 *   - commitMessage : commit message to use
 *   - files         : JSON string array of objects { "path": "relative/path/to/file", "content": "file contents" }
 *
 * The tool reads the token from the environment variable `GITHUB_API_TOKEN`.  If the token is missing,
 * the tool returns a failure result.
 *
 * NOTE: This is a minimal implementation that uses the GitHub REST API v3.
 *       It does NOT handle large files, binary data, or sophisticated conflict resolution.
 */
class GitHubIntegrationTool : ToolContract {

    override val name: String = "GitHubIntegration"
    override val description: String =
        "Commits provided files to a GitHub repository using the configured API token and triggers the workflow."

    private val client = OkHttpClient()

    override suspend fun execute(args: Map<String, String>): ToolResult = withContext(Dispatchers.IO) {
        val token = System.getenv("GITHUB_API_TOKEN")
            ?: return@withContext ToolResult.Failure("Environment variable GITHUB_API_TOKEN not set.")

        val repo = args["repo"] ?: return@withContext ToolResult.Failure("Missing required arg: repo")
        val branch = args["branch"] ?: "main"
        val commitMessage = args["commitMessage"] ?: "Automated commit by Chintu"
        val filesJson = args["files"] ?: return@withContext ToolResult.Failure("Missing required arg: files")

        // Parse files JSON (simple parsing, expecting format: [{"path":"a/b.txt","content":"..."}])
        val files = try {
            parseFilesJson(filesJson)
        } catch (e: Exception) {
            return@withContext ToolResult.Failure("Failed to parse 'files' argument: ${e.message}")
        }

        // 1️⃣ Get the latest commit SHA of the target branch
        val refInfo = githubGet(
            url = "https://api.github.com/repos/$repo/git/refs/heads/$branch",
            token = token
        ) ?: return@withContext ToolResult.Failure("Unable to fetch branch reference.")

        val latestCommitSha = refInfo["object"]?.let { (it as Map<*, *>)["sha"] as String }
            ?: return@withContext ToolResult.Failure("Could not determine latest commit SHA.")

        // 2️⃣ Create a new tree with the supplied files
        val treeSha = createTree(repo, token, files, baseTreeSha = getTreeSha(repo, token, latestCommitSha))
            ?: return@withContext ToolResult.Failure("Failed to create a new tree.")

        // 3️⃣ Create a new commit object
        val newCommitSha = createCommit(
            repo = repo,
            token = token,
            message = commitMessage,
            treeSha = treeSha,
            parentSha = latestCommitSha
        ) ?: return@withContext ToolResult.Failure("Failed to create a new commit.")

        // 4️⃣ Update the branch reference to point to the new commit
        val updateResult = githubPatch(
            url = "https://api.github.com/repos/$repo/git/refs/heads/$branch",
            token = token,
            jsonBody = """{ "sha": "$newCommitSha", "force": false }"""
        ) ?: return@withContext ToolResult.Failure("Failed to update branch reference.")

        // 5️⃣ (Optional) Trigger workflow – GitHub Actions runs automatically on push.
        // We simply return success here.
        ToolResult.Success(
            "Committed ${files.size} file(s) to $repo@$branch with SHA $newCommitSha. " +
                    "GitHub Actions workflow will be triggered automatically."
        )
    }

    // -------------------------------------------------------------------------
    // Helper data structures and JSON parsing (using kotlinx.serialization would be ideal,
    // but we keep it lightweight with minimal manual parsing to avoid adding dependencies).
    // -------------------------------------------------------------------------
    data class FileBlob(val path: String, val content: String)

    private fun parseFilesJson(json: String): List<FileBlob> {
        // Very naive parser – expects an array of objects with string fields "path" and "content".
        // For production, replace with proper JSON library (e.g., kotlinx.serialization or Moshi).
        val trimmed = json.trim()
        require(trimmed.startsWith("[") && trimmed.endsWith("]")) { "JSON array expected." }
        val items = trimmed.removePrefix("[").removeSuffix("]").split("},").map {
            var s = it.trim()
            if (!s.endsWith("}")) s += "}"
            s
        }

        return items.map { item ->
            val path = "\"path\"".toRegex().find(item)?.let {
                "\"".toRegex().replaceFirst(it.value, "").substringAfter(":").trim().trim('"')
            } ?: throw IllegalArgumentException("Missing 'path' in $item")

            val content = "\"content\"".toRegex().find(item)?.let {
                "\"".toRegex().replaceFirst(it.value, "").substringAfter(":").trim().trim('"')
            } ?: throw IllegalArgumentException("Missing 'content' in $item")

            FileBlob(path, content)
        }
    }

    // -------------------------------------------------------------------------
    // GitHub API helpers
    // -------------------------------------------------------------------------
    private fun githubGet(url: String, token: String): Map<String, Any>? {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "token $token")
            .addHeader("Accept", "application/vnd.github.v3+json")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            @Suppress("UNCHECKED_CAST")
            return jsonToMap(body) as Map<String, Any>
        }
    }

    private fun githubPost(url: String, token: String, jsonBody: String): Map<String, Any>? {
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonBody)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "token $token")
            .addHeader("Accept", "application/vnd.github.v3+json")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val respBody = response.body?.string() ?: return null
            @Suppress("UNCHECKED_CAST")
            return jsonToMap(respBody) as Map<String, Any>
        }
    }

    private fun githubPatch(url: String, token: String, jsonBody: String): Map<String, Any>? {
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonBody)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "token $token")
            .addHeader("Accept", "application/vnd.github.v3+json")
            .patch(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val respBody = response.body?.string() ?: return null
            @Suppress("UNCHECKED_CAST")
            return jsonToMap(respBody) as Map<String, Any>
        }
    }

    // -------------------------------------------------------------------------
    // GitHub specific steps
    // -------------------------------------------------------------------------
    private fun getTreeSha(repo: String, token: String, commitSha: String): String? {
        val commitInfo = githubGet(
            url = "https://api.github.com/repos/$repo/git/commits/$commitSha",
            token = token
        ) ?: return null
        return (commitInfo["tree"] as? Map<*, *>)?.get("sha") as? String
    }

    private fun createTree(
        repo: String,
        token: String,
        files: List<FileBlob>,
        baseTreeSha: String?
    ): String? {
        // Build array of tree objects
        val treeEntries = files.joinToString(separator = ",") { file ->
            val encoded = Base64.getEncoder().encodeToString(file.content.toByteArray(UTF_8))
            """{
                "path": "${file.path}",
                "mode": "100644",
                "type": "blob",
                "content": "${file.content.replace("\\", "\\\\").replace("\"", "\\\"")}"
            }"""
        }

        val json = StringBuilder()
            .append("{")
            .append("\"base_tree\": \"$baseTreeSha\",")
            .append("\"tree\": [")
            .append(treeEntries)
            .append("]")
            .append("}")
            .toString()

        val resp = githubPost(
            url = "https://api.github.com/repos/$repo/git/trees",
            token = token,
            jsonBody = json
        ) ?: return null

        return resp["sha"] as? String
    }

    private fun createCommit(
        repo: String,
        token: String,
        message: String,
        treeSha: String,
        parentSha: String
    ): String? {
        val json = """{
            "message": "${message.replace("\\", "\\\\").replace("\"", "\\\"")}",
            "tree": "$treeSha",
            "parents": ["$parentSha"]
        }"""
        val resp = githubPost(
            url = "https://api.github.com/repos/$repo/git/commits",
            token = token,
            jsonBody = json
        ) ?: return null
        return resp["sha"] as? String
    }

    // -------------------------------------------------------------------------
    // Very light JSON → Map parser (only for objects and primitives we need)
    // -------------------------------------------------------------------------
    private fun jsonToMap(json: String): Any? {
        // For brevity we rely on the built‑in JavaScript engine if available.
        // In a real project replace this with a proper JSON library.
        val script = """
            Java.asJSONCompatible($json)
        """.trimIndent()
        return try {
            val engine = javax.script.ScriptEngineManager().getEngineByName("javascript")
            engine?.eval(script)
        } catch (e: Exception) {
            null
        }
    }
}