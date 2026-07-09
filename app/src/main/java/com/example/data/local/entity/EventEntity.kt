package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [Index(value = ["deterministicHash"], unique = true)]
)
data class EventEntity(
    @PrimaryKey
    val localEventId: String,
    val schemaVersion: Int,
    val connectorType: String,
    val connectorVersion: String,
    val payloadJson: String,
    val deterministicHash: String,
    val state: String,
    val retryCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val acknowledgedAt: Long?,
    val backendEventId: String?,
    val failureReason: String?
)
