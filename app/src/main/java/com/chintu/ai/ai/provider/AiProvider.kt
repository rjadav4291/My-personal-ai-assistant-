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

    override val name: String = "Google Gemini"
    private val client = HttpClient(Android)

    override suspend fun isConfigured(): Boolean {
        val key = secureStorage.getString("GEMINI_API_KEY")
        return !key.isNullOrBlank()
    }

    override suspend fun execute(request: AiRequest): Result<AiResponse> {
        val apiKey = secureStorage.getString("GEMINI_API_KEY")
            ?: return Result.failure(IllegalStateException("GEMINI_API_KEY configure nathi. Settings ma jaaine key add karo."))

        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

            val contentsJson = buildJsonArray {
                request.messages.forEach { msg ->
                    addJsonObject {
                        put("role", if (msg.role == MessageRole.USER) "user" else "model")
                        putJsonObject("parts") {
                            put("text", msg.content)
                        }
                    }
                }
            }

            val payload = buildJsonObject {
                put("contents", contentsJson)
                putJsonObject("systemInstruction") {
                    putJsonObject("parts") {
                        put("text", request.systemInstruction)
                    }
                }
            }

            val httpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            if (httpResponse.status.isSuccess()) {
                val root = Json.parseToJsonElement(httpResponse.bodyAsText()).jsonObject
                val text = root["candidates"]?.jsonArray?.getOrNull(0)?.jsonObject
                    ?.get("content")?.jsonObject
                    ?.get("parts")?.jsonArray?.getOrNull(0)?.jsonObject
                    ?.get("text")?.jsonPrimitive?.content ?: "Koi response malyo nathi."

                Result.success(AiResponse(text = text, provider = name, isSuccess = true))
            } else {
                Result.failure(Exception("Gemini HTTP Error: ${httpResponse.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
