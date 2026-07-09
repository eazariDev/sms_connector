package com.example.data.remote

import com.example.data.remote.model.AuthResponse
import com.example.data.remote.model.RefreshTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse
}
