package com.example.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.repository.EventRepository

class AppWorkerFactory(
    private val eventRepository: EventRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            EventUploadWorker::class.java.name ->
                EventUploadWorker(appContext, workerParameters, eventRepository)
            ConfigSyncWorker::class.java.name ->
                ConfigSyncWorker(appContext, workerParameters, eventRepository)
            else -> null
        }
    }
}
