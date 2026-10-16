package com.chintu.ai.memory

import android.content.Context
import com.chintu.ai.core.security.SecureStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MemoryManager(
    private val context: Context,
    private val secureStorage: SecureStorage
) {
    // Persistent Conversation Storage (Chat History Save & Load)
    fun saveConversation(messages: List<String>) {
        try {
            val jsonString = Json.encodeToString(messages)
            secureStorage.saveString("CHAT_HISTORY", jsonString)
        } catch (_: Exception) {}
    }

    fun loadConversation(): List<String> {
        val jsonString = secureStorage.getString("CHAT_HISTORY") ?: return emptyList()
        return try {
            Json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Agent Short-term & Context Memory
    fun remember(key: String, value: String) {
        secureStorage.saveString("MEM_$key", value)
    }

    fun recall(key: String): String? {
        return secureStorage.getString("MEM_$key")
    }

    fun clearMemory() {
        secureStorage.saveString("CHAT_HISTORY", "")
    }
}
