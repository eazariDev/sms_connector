package com.example.data.local.entity

enum class EventState {
    RECEIVED, QUEUED, UPLOADING, UPLOADED, ACKNOWLEDGED, FAILED, DEAD_LETTER
}
