package com.example.repository

import com.example.core.queue.UnifiedEventQueue
import com.example.data.local.DataStoreManager
import com.example.data.local.dao.EventDao
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.EventState
import com.example.data.remote.EventApiService
import com.example.data.remote.model.ConnectorEvent
import com.example.data.remote.model.ConnectorMetadata
import com.example.data.remote.model.DeviceMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.util.UUID

class EventRepository(
    private val eventDao: EventDao,
    private val apiService: EventApiService,
    private val dataStoreManager: DataStoreManager
) : UnifiedEventQueue {
    
    private val json = Json { ignoreUnknownKeys = true }

    fun getAcknowledgedCountFlow(): Flow<Int> = eventDao.getAcknowledgedCountFlow()
    fun getPendingCountFlow(): Flow<Int> = eventDao.getPendingCountFlow()
    fun getDeadLetterCountFlow(): Flow<Int> = eventDao.getDeadLetterCountFlow()

    override suspend fun enqueueEvent(
        connectorType: String,
        connectorVersion: String,
        payloadJson: String,
        deterministicHash: String
    ) {
        val now = System.currentTimeMillis()
        val localEventId = UUID.randomUUID().toString()
        
        val event = EventEntity(
            localEventId = localEventId,
            schemaVersion = 1,
            connectorType = connectorType,
            connectorVersion = connectorVersion,
            payloadJson = payloadJson,
            deterministicHash = deterministicHash,
            state = EventState.QUEUED.name,
            retryCount = 0,
            createdAt = now,
            updatedAt = now,
            acknowledgedAt = null,
            backendEventId = null,
            failureReason = null
        )
        try {
            eventDao.insertEvent(event)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun uploadPendingEvents() {
        val pendingList = eventDao.getPendingEvents()
        if (pendingList.isEmpty()) return

        val deviceId = dataStoreManager.getDeviceId()
        val deviceMetadata = DeviceMetadata(
            deviceId = deviceId,
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            androidVersion = android.os.Build.VERSION.RELEASE,
            appVersion = "1.0.0-RELEASE"
        )
        
        for (event in pendingList) {
            eventDao.updateEventState(event.localEventId, EventState.UPLOADING.name, System.currentTimeMillis())
            
            try {
                val payloadElement: JsonElement = json.parseToJsonElement(event.payloadJson)
                val connectorEvent = ConnectorEvent(
                    schemaVersion = event.schemaVersion,
                    connector = ConnectorMetadata(event.connectorType, event.connectorVersion),
                    device = deviceMetadata,
                    payload = payloadElement,
                    localEventId = event.localEventId,
                    createdAt = event.createdAt
                )
                
                val response = apiService.uploadEvent(
                    idempotencyKey = event.deterministicHash,
                    event = connectorEvent
                )
                
                if (response.accepted || response.alreadyExists) {
                    eventDao.markAcknowledged(
                        id = event.localEventId,
                        state = EventState.ACKNOWLEDGED.name,
                        backendEventId = response.eventId,
                        acknowledgedAt = response.serverTimestamp,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    handleFailure(event, "Server rejected event")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleFailure(event, e.message ?: "Unknown network error")
            }
        }
    }

    private suspend fun handleFailure(event: EventEntity, reason: String) {
        val newRetry = event.retryCount + 1
        val newState = if (newRetry >= 20) EventState.DEAD_LETTER else EventState.FAILED
        eventDao.updateFailure(
            id = event.localEventId,
            retryCount = newRetry,
            state = newState.name,
            reason = reason,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun refreshConfig() {
        try {
            val rules = apiService.getSenderRules()
            dataStoreManager.setSenderRules(rules)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
