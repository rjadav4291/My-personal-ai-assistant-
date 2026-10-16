package com.chintu.ai.memory

import android.content.Context
import com.chintu.ai.core.security.SecureStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MemoryManager(
    private val context: Context,
    private val secureStorage: SecureStorage? = null
) {
    private val memoryMap = mutableMapOf<String, String>()

    fun saveConversation(messages: List<String>) {
        try {
            val jsonString = Json.encodeToString(messages)
            secureStorage?.saveString("CHAT_HISTORY", jsonString)
        } catch (_: Exception) {}
    }

    fun loadConversation(): List<String> {
        val jsonString = secureStorage?.getString("CHAT_HISTORY") ?: return emptyList()
        return try {
            Json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun remember(key: String, value: String) {
        memoryMap[key] = value
        secureStorage?.saveString("MEM_$key", value)
    }

    fun recall(key: String): String? {
        return memoryMap[key] ?: secureStorage?.getString("MEM_$key")
    }

    fun getAllMemories(): Map<String, String> {
        return memoryMap.toMap()
    }

    fun clearMemories() {
        memoryMap.clear()
        secureStorage?.saveString("CHAT_HISTORY", "")
    }

    fun clearMemory() {
        clearMemories()
    }
}
