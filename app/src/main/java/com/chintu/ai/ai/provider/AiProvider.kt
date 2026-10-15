package com.chintu.ai.ai.provider

import com.chintu.ai.ai.model.AiRequest
import com.chintu.ai.ai.model.AiResponse

interface AiProvider {
    val name: String
    suspend fun isConfigured(): Boolean
    suspend fun execute(request: AiRequest): Result<AiResponse>
}
