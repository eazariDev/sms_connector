package com.example.data.remote

import com.example.data.remote.model.ConnectorEvent
import com.example.data.remote.model.EventUploadResponse
import com.example.data.remote.model.SenderRule
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface EventApiService {
    @POST("api/v1/events/upload")
    suspend fun uploadEvent(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body event: ConnectorEvent
    ): EventUploadResponse

    @GET("api/v1/config/senders")
    suspend fun getSenderRules(): List<SenderRule>
}
