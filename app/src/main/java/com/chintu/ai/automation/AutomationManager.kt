package com.chintu.ai.automation

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class AutomationManager(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleTask(taskName: String, delayMinutes: Long) {
        val data = workDataOf("TASK_NAME" to taskName)
        val request = OneTimeWorkRequestBuilder<AutomationWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(data)
            .build()

        workManager.enqueue(request)
    }
}
