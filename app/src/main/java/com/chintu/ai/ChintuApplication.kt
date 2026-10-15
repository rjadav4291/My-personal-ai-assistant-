package com.chintu.ai

import android.app.Application
import com.chintu.ai.core.security.SecureStorage
import com.chintu.ai.memory.MemoryManager
import com.chintu.ai.tools.AndroidSystemTools
import com.chintu.ai.tools.ToolRegistry

class ChintuApplication : Application() {

    lateinit var secureStorage: SecureStorage
        private set
    lateinit var memoryManager: MemoryManager
        private set
    lateinit var toolRegistry: ToolRegistry
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        secureStorage = SecureStorage(applicationContext)
        memoryManager = MemoryManager(applicationContext)
        toolRegistry = ToolRegistry()

        val systemTools = AndroidSystemTools(applicationContext)
        toolRegistry.register(systemTools.getBatteryTool())
        toolRegistry.register(systemTools.getLaunchAppTool())
    }

    companion object {
        lateinit var instance: ChintuApplication
            private set
    }
}
