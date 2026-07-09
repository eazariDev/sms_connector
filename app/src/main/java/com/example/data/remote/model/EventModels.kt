package com.example.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ConnectorMetadata(
    val type: String,
    @SerialName("connector_version") val connectorVersion: String
)

@Serializable
data class DeviceMetadata(
    @SerialName("device_id") val deviceId: String,
    val manufacturer: String,
    val model: String,
    @SerialName("android_version") val androidVersion: String,
    @SerialName("app_version") val appVersion: String
)

@Serializable
data class ConnectorEvent(
    @SerialName("schema_version") val schemaVersion: Int,
    val connector: ConnectorMetadata,
    val device: DeviceMetadata,
    val payload: JsonElement,
    @SerialName("local_event_id") val localEventId: String,
    @SerialName("created_at") val createdAt: Long
)

@Serializable
data class EventUploadResponse(
    val accepted: Boolean,
    @SerialName("already_exists") val alreadyExists: Boolean,
    @SerialName("event_id") val eventId: String,
    @SerialName("server_timestamp") val serverTimestamp: Long,
    @SerialName("server_schema_version") val serverSchemaVersion: Int
)

@Serializable
data class SenderRule(
    val type: String,
    val value: String
)
