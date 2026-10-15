package com.chintu.ai.memory

import android.content.Context
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MemoryEntry(
    val id: String,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MemoryManager(context: Context) {
    private val memoryFile = File(context.filesDir, "chintu_memory.json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Synchronized
    fun getAllMemories(): List<MemoryEntry> {
        if (!memoryFile.exists()) return emptyList()
        return try {
            json.decodeFromString<List<MemoryEntry>>(memoryFile.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun saveMemory(key: String, value: String) {
        val current = getAllMemories().toMutableList()
        current.removeAll { it.key.equals(key, ignoreCase = true) }
        current.add(MemoryEntry(id = System.currentTimeMillis().toString(), key = key, value = value))
        memoryFile.writeText(json.encodeToString(current))
    }

    @Synchronized
    fun clearMemories() {
        if (memoryFile.exists()) {
            memoryFile.delete()
        }
    }
}
