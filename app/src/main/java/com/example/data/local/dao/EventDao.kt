package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: EventEntity): Long

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE state = 'QUEUED' OR state = 'FAILED' ORDER BY createdAt ASC LIMIT 50")
    suspend fun getPendingEvents(): List<EventEntity>

    @Query("UPDATE events SET state = :state, updatedAt = :updatedAt WHERE localEventId = :id")
    suspend fun updateEventState(id: String, state: String, updatedAt: Long)

    @Query("UPDATE events SET state = :state, acknowledgedAt = :acknowledgedAt, backendEventId = :backendEventId, updatedAt = :updatedAt WHERE localEventId = :id")
    suspend fun markAcknowledged(id: String, state: String, backendEventId: String, acknowledgedAt: Long, updatedAt: Long)

    @Query("UPDATE events SET retryCount = :retryCount, state = :state, failureReason = :reason, updatedAt = :updatedAt WHERE localEventId = :id")
    suspend fun updateFailure(id: String, retryCount: Int, state: String, reason: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM events WHERE state = 'ACKNOWLEDGED'")
    fun getAcknowledgedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE state IN ('QUEUED', 'FAILED', 'UPLOADING', 'RECEIVED')")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE state = 'DEAD_LETTER'")
    fun getDeadLetterCountFlow(): Flow<Int>
}
