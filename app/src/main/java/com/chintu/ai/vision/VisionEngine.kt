package com.chintu.ai.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.chintu.ai.core.security.SecureStorage
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.io.ByteArrayOutputStream

class VisionEngine(
    private val context: Context,
    private val secureStorage: SecureStorage
) {
    private val client = HttpClient(Android)

    suspend fun analyzeImage(imageUri: Uri, prompt: String = "આ ફોટામાં શું છે તે વિગતવાર સમજાવો."): Result<String> {
        val apiKey = secureStorage.getString("GEMINI_API_KEY")
            ?: return Result.failure(IllegalStateException("GEMINI_API_KEY સેટ કરેલ નથી."))

        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

            val payload = buildJsonObject {
                putJsonArray("contents") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            addJsonObject {
                                put("text", prompt)
                            }
                            addJsonObject {
                                putJsonObject("inlineData") {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }
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
                    ?.get("text")?.jsonPrimitive?.content ?: "ઈમેજ એનાલિસિસ થઈ શક્યું નહીં."
                Result.success(text)
            } else {
                Result.failure(Exception("Vision API Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
