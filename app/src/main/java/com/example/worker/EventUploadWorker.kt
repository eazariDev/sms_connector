package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.repository.EventRepository

class EventUploadWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val eventRepository: EventRepository
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            eventRepository.uploadPendingEvents()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
