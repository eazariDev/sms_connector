package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.EventDao
import com.example.data.local.entity.EventEntity

@Database(entities = [EventEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val eventDao: EventDao
}
