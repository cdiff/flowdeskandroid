package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.domain.model.User

import com.example.flowdesk_android.data.remote.dto.AuthMeResponse

interface AuthRepository {
    suspend fun login(tenantName: String, userId: String, password: String): Result<User>
    suspend fun signUp(companyName: String, adminName: String, email: String, phone: String, password: String): Result<String>
    suspend fun getMe(): Result<AuthMeResponse>
    suspend fun updateProfile(userName: String, userEmail: String, userTel: String?, userHp: String?): Result<com.example.flowdesk_android.data.remote.dto.UserDto>
    suspend fun changePassword(current: String, new: String, confirm: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun logoutAll(): Result<Unit>
}
