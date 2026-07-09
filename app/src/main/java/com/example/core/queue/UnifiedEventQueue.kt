package com.example.core.queue

interface UnifiedEventQueue {
    suspend fun enqueueEvent(
        connectorType: String,
        connectorVersion: String,
        payloadJson: String,
        deterministicHash: String
    )
}
