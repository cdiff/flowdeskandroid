package com.example.flowdesk_android.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val id: String,
    val email: String,
    val name: String,
    val accessToken: String
)
