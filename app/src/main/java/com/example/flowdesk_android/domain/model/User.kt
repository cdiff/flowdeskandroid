package com.example.flowdesk_android.domain.model

data class User(
    val id: String,
    val userId: String,
    val email: String,
    val name: String,
    val corpName: String,
    val token: String
)
