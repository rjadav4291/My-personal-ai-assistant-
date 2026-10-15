package com.chintu.ai

import android.app.Application
import com.chintu.ai.ai.provider.GeminiProvider
import com.chintu.ai.core.security.SecureStorage
import com.chintu.ai.development.GitHubService
import com.chintu.ai.development.SelfDevelopmentManager
import com.chintu.ai.memory.MemoryManager
import com.chintu.ai.skills.SkillManager
import com.chintu.ai.tools.AndroidSystemTools
import com.chintu.ai.tools.ToolRegistry

class ChintuApplication : Application() {

    lateinit var secureStorage: SecureStorage
        private set
    lateinit var memoryManager: MemoryManager
        private set
    lateinit var skillManager: SkillManager
        private set
    lateinit var toolRegistry: ToolRegistry
        private set
    lateinit var gitHubService: GitHubService
        private set
    lateinit var selfDevelopmentManager: SelfDevelopmentManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        secureStorage = SecureStorage(applicationContext)
        memoryManager = MemoryManager(applicationContext)
        skillManager = SkillManager(applicationContext)
        toolRegistry = ToolRegistry()

        val systemTools = AndroidSystemTools(applicationContext)
        toolRegistry.register(systemTools.getBatteryTool())
        toolRegistry.register(systemTools.getLaunchAppTool())

        gitHubService = GitHubService(secureStorage)
        val aiProvider = GeminiProvider(secureStorage)
        selfDevelopmentManager = SelfDevelopmentManager(gitHubService, aiProvider)
    }

    companion object {
        lateinit var instance: ChintuApplication
            private set
    }
}
