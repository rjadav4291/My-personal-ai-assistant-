package com.chintu.ai

import android.app.Application
import com.chintu.ai.ai.provider.GeminiProvider
import com.chintu.ai.core.security.SecureStorage
import com.chintu.ai.development.GitHubService
import com.chintu.ai.development.SelfDevelopmentManager
import com.chintu.ai.memory.MemoryManager
import com.chintu.ai.privacy.PrivacyCenter
import com.chintu.ai.skills.SkillManager
import com.chintu.ai.tools.ToolRegistry
import com.chintu.ai.vision.VisionEngine

class ChintuApplication : Application() {

    lateinit var secureStorage: SecureStorage
    lateinit var memoryManager: MemoryManager
    lateinit var toolRegistry: ToolRegistry
    lateinit var skillManager: SkillManager
    lateinit var gitHubService: GitHubService
    lateinit var selfDevelopmentManager: SelfDevelopmentManager
    lateinit var privacyCenter: PrivacyCenter
    lateinit var visionEngine: VisionEngine

    override fun onCreate() {
        super.onCreate()

        secureStorage = SecureStorage(this)
        memoryManager = MemoryManager(this, secureStorage)
        toolRegistry = ToolRegistry(this)
        skillManager = SkillManager(this)
        gitHubService = GitHubService(secureStorage)
        
        val aiProvider = GeminiProvider(secureStorage)
        selfDevelopmentManager = SelfDevelopmentManager(gitHubService, aiProvider)
        privacyCenter = PrivacyCenter(memoryManager)
        visionEngine = VisionEngine(this)
    }
}
