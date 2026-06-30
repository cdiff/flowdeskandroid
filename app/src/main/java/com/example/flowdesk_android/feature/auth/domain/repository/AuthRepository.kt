package com.example.flowdesk_android.feature.auth.domain.repository

import com.example.flowdesk_android.feature.auth.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionState: StateFlow<AuthSession>

    suspend fun login(command: LoginCommand): Result<Unit>
    suspend fun initializeSession()
    suspend fun signUp(tenantName: String, companyName: String, adminName: String, email: String, phone: String, password: String): Result<String>
    suspend fun getMe(): Result<AuthMeInfo>
    suspend fun updateProfile(userName: String, userEmail: String, userTel: String?, userHp: String?): Result<AuthUser>
    suspend fun changePassword(current: String, new: String, confirm: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun logoutAll(): Result<Unit>
}

