package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.data.remote.dto.UserDto

interface UserRepository {
    suspend fun getUsers(): Result<List<UserDto>>
    suspend fun createUser(request: com.example.flowdesk_android.data.remote.dto.CreateUserRequest): Result<UserDto>
}
