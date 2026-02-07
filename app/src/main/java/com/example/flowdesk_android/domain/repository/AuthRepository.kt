package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
}
