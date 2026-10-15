package com.chintu.ai.ai.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

@Serializable
data class AiMessage(
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiRequest(
    val messages: List<AiMessage>,
    val systemInstruction: String = "You are CHINTU: an intelligent, calm, truthful Android AI assistant. Respond helpfully in Gujarati, Hindi, English, or Hinglish as requested."
)

data class AiResponse(
    val text: String,
    val provider: String,
    val isSuccess: Boolean
)
