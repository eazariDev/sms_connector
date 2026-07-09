package com.example.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val deviceId: String,
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)
