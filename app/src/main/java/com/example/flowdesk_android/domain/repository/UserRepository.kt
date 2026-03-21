package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.data.remote.dto.UserDto

interface UserRepository {
    suspend fun getUsers(): Result<List<UserDto>>
    suspend fun createUser(request: com.example.flowdesk_android.data.remote.dto.CreateUserRequest): Result<UserDto>
    suspend fun getUserDetail(id: Int): Result<com.example.flowdesk_android.data.remote.dto.UserDetailDto>
    suspend fun updateUserStatus(id: Int, isActive: Int): Result<Unit>
    suspend fun updateUserRoles(id: Int, request: com.example.flowdesk_android.data.remote.dto.UpdateUserRolesRequest): Result<Unit>
    suspend fun adminChangePassword(id: Int, newPassword: String): Result<Unit>
    suspend fun invalidateUserTokens(id: Int): Result<Unit>
    suspend fun updateUser(id: Int, request: com.example.flowdesk_android.data.remote.dto.UpdateUserInfoRequest): Result<Unit>
}
