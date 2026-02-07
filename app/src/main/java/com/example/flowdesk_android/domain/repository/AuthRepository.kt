package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.domain.model.User

interface AuthRepository {
    suspend fun login(tenantName: String, userId: String, password: String): Result<User>
}
