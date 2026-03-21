package com.example.flowdesk_android.data.repository

import com.example.flowdesk_android.data.remote.UserApi
import com.example.flowdesk_android.data.remote.dto.AdminChangePasswordRequest
import com.example.flowdesk_android.data.remote.dto.CreateUserRequest
import com.example.flowdesk_android.data.remote.dto.UpdateUserInfoRequest
import com.example.flowdesk_android.data.remote.dto.UpdateUserStatusRequest
import com.example.flowdesk_android.data.remote.dto.UpdateUserRolesRequest
import com.example.flowdesk_android.data.remote.dto.UserDetailDto
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

    override suspend fun getUserDetail(id: Int): Result<com.example.flowdesk_android.data.remote.dto.UserDetailDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch user details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserStatus(id: Int, isActive: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateUserStatus(id, com.example.flowdesk_android.data.remote.dto.UpdateUserStatusRequest(isActive))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRoles(id: Int, request: UpdateUserRolesRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateUserRoles(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update roles: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun adminChangePassword(id: Int, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.adminChangePassword(id, com.example.flowdesk_android.data.remote.dto.AdminChangePasswordRequest(newPassword))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to change password: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun invalidateUserTokens(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.invalidateUserTokens(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to invalidate tokens: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(id: Int, request: com.example.flowdesk_android.data.remote.dto.UpdateUserInfoRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateUser(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
