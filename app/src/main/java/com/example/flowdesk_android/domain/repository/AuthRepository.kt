package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.domain.model.User

interface AuthRepository {
    suspend fun login(tenantName: String, userId: String, password: String): Result<User>
    suspend fun signUp(companyName: String, adminName: String, email: String, phone: String, password: String): Result<String>
}
