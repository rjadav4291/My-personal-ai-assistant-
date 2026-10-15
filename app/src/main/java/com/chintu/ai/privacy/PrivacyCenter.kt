package com.chintu.ai.privacy

import com.chintu.ai.core.security.SecureStorage
import com.chintu.ai.memory.MemoryManager

class PrivacyCenter(
    private val secureStorage: SecureStorage,
    private val memoryManager: MemoryManager
) {
    fun wipeAllUserData() {
        memoryManager.clearMemories()
        secureStorage.remove("GEMINI_API_KEY")
        secureStorage.remove("GITHUB_PAT_TOKEN")
    }

    fun getPrivacyAuditSummary(): Map<String, String> {
        val memoriesCount = memoryManager.getAllMemories().size
        val hasGemini = !secureStorage.getString("GEMINI_API_KEY").isNullOrBlank()
        val hasGitHub = !secureStorage.getString("GITHUB_PAT_TOKEN").isNullOrBlank()

        return mapOf(
            "Persistent Memories Stored" to "$memoriesCount items",
            "Gemini Key Stored" to if (hasGemini) "Encrypted (AES-256)" else "Not Configured",
            "GitHub Token Stored" to if (hasGitHub) "Encrypted (AES-256)" else "Not Configured"
        )
    }
}
