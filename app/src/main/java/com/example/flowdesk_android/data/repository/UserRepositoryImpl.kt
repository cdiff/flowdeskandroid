package com.example.flowdesk_android.data.repository

import com.example.flowdesk_android.data.remote.UserApi
import com.example.flowdesk_android.data.remote.dto.UserDto
import com.example.flowdesk_android.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {

    override suspend fun getUsers(): Result<List<UserDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUsers()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.items ?: emptyList())
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch users: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(request: com.example.flowdesk_android.data.remote.dto.CreateUserRequest): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.createUser(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to create user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
