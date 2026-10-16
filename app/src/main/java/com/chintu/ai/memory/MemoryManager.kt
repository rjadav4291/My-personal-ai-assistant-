package com.chintu.ai.memory

import android.content.Context
import com.chintu.ai.core.security.SecureStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MemoryStorage(context: Context, private val secureStorage: SecureStorage) {

    fun saveConversation(messages: List<String>) {
        val jsonString = Json.encodeToString(messages)
        secureStorage.saveString("CHAT_HISTORY", jsonString)
    }

    fun loadConversation(): List<String> {
        val jsonString = secureStorage.getString("CHAT_HISTORY") ?: return emptyList()
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
