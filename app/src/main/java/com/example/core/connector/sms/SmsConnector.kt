package com.example.core.connector.sms

import com.example.core.queue.UnifiedEventQueue
import com.example.data.local.DataStoreManager
import com.example.data.remote.model.SenderRule
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

@Serializable
data class SmsPayload(
    val sender: String,
    val body: String,
    val smsTimestamp: Long
)

class SmsConnector(
    private val eventQueue: UnifiedEventQueue,
    private val dataStoreManager: DataStoreManager,
    private val notificationManager: com.example.core.notification.AppNotificationManager
) {
    suspend fun processIncomingSms(sender: String, body: String, timestamp: Long) {
        val rules = dataStoreManager.getSenderRules()
        if (!isAllowed(sender, rules)) return

        val payload = SmsPayload(sender, body, timestamp)
        val payloadJson = Json.encodeToString(payload)
        val hashStr = "$sender$body$timestamp"
        val deterministicHash = generateSHA256(hashStr)
        
        eventQueue.enqueueEvent(
            connectorType = "SMS",
            connectorVersion = "1.0",
            payloadJson = payloadJson,
            deterministicHash = deterministicHash
        )
        notificationManager.showSmsQueuedNotification(sender)
    }
    
    private fun isAllowed(sender: String, rules: List<SenderRule>): Boolean {
        if (rules.isEmpty()) return false
        for (rule in rules) {
            when (rule.type.uppercase()) {
                "EXACT" -> if (sender == rule.value) return true
                "PREFIX" -> if (sender.startsWith(rule.value)) return true
                "REGEX" -> if (Regex(rule.value).matches(sender)) return true
                else -> if (sender == rule.value) return true
            }
        }
        return false
    }
    
    private fun generateSHA256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
