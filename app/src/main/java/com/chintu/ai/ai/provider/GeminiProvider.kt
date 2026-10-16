package com.chintu.ai.ai.provider

import com.chintu.ai.ai.model.AiMessage
import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.AiResponse
import com.chintu.ai.ai.model.MessageRole
import com.chintu.ai.core.security.SecureStorage
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class GeminiProvider(
    private val secureStorage: SecureStorage
) : AiProvider {

    override val name: String = "Multi-Engine AI"
    private val client = HttpClient(Android)

    override suspend fun isConfigured(): Boolean {
        val key = secureStorage.getString("API_KEY") ?: secureStorage.getString("GEMINI_API_KEY")
        return !key.isNullOrBlank()
    }

    override suspend fun execute(request: AiRequest): Result<AiResponse> {
        val apiKey = (secureStorage.getString("API_KEY") ?: secureStorage.getString("GEMINI_API_KEY"))?.trim()
            ?: return Result.failure(IllegalStateException("API Key સેટ કરેલ નથી. સેટિંગ્સમાં ઉમેરો."))

        val customServer = secureStorage.getString("SERVER_ENDPOINT")?.trim()
        val customModel = secureStorage.getString("MODEL_NAME")?.trim() ?: "gemini-1.5-flash"

        return try {
            if (!customServer.isNullOrBlank() && (customServer.contains("/v1") || customServer.contains("chat/completions"))) {
                // OpenAI / Custom Compatible Server Mode (Groq, OpenRouter, Local Ollama)
                val messagesJson = buildJsonArray {
                    if (request.systemInstruction.isNotBlank()) {
                        addJsonObject {
                            put("role", "system")
                            put("content", request.systemInstruction)
                        }
                    }
                    request.messages.forEach { msg ->
                        addJsonObject {
                            put("role", if (msg.role == MessageRole.USER) "user" else "assistant")
                            put("content", msg.content)
                        }
                    }
                }

                val payload = buildJsonObject {
                    put("model", customModel)
                    put("messages", messagesJson)
                }

                val response = client.post(customServer) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $apiKey")
                    setBody(payload.toString())
                }

                if (response.status.isSuccess()) {
                    val root = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    val text = root["choices"]?.jsonArray?.getOrNull(0)?.jsonObject
                        ?.get("message")?.jsonObject
                        ?.get("content")?.jsonPrimitive?.content ?: "ખાલી જવાબ મળ્યો."
                    Result.success(AiResponse(text = text, provider = name, isSuccess = true))
                } else {
                    Result.failure(Exception("Custom Server Error: ${response.status.value} - ${response.bodyAsText()}"))
                }
            } else {
                // Google Gemini Official Direct Mode
                val cleanModel = if (customModel.isNotBlank()) customModel else "gemini-1.5-flash"
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$apiKey"

                val contentsJson = buildJsonArray {
                    request.messages.forEach { msg ->
                        addJsonObject {
                            put("role", if (msg.role == MessageRole.USER) "user" else "model")
                            putJsonArray("parts") {
                                addJsonObject {
                                    put("text", msg.content)
                                }
                            }
                        }
                    }
                }

                val payload = buildJsonObject {
                    put("contents", contentsJson)
                    if (request.systemInstruction.isNotBlank()) {
                        putJsonObject("systemInstruction") {
                            putJsonArray("parts") {
                                addJsonObject {
                                    put("text", request.systemInstruction)
                                }
                            }
                        }
                    }
                }

                val response = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }

                if (response.status.isSuccess()) {
                    val root = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    val text = root["candidates"]?.jsonArray?.getOrNull(0)?.jsonObject
                        ?.get("content")?.jsonObject
                        ?.get("parts")?.jsonArray?.getOrNull(0)?.jsonObject
                        ?.get("text")?.jsonPrimitive?.content ?: "કોઈ જવાબ મળ્યો નથી."
                    Result.success(AiResponse(text = text, provider = name, isSuccess = true))
                } else {
                    Result.failure(Exception("Gemini HTTP Error: ${response.status.value} - ${response.bodyAsText()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
