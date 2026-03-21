package com.example.flowdesk_android.data.repository

import com.example.flowdesk_android.data.remote.RoleApi
import com.example.flowdesk_android.data.remote.dto.CreateRoleRequest
import com.example.flowdesk_android.data.remote.dto.RoleDto
import com.example.flowdesk_android.domain.repository.RoleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRepositoryImpl @Inject constructor(
    private val api: RoleApi
) : RoleRepository {

    override suspend fun getRoles(): Result<List<RoleDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRoles()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.items ?: emptyList())
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch roles: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRole(request: CreateRoleRequest): Result<Unit> {
        return try {
            val response = api.createRole(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create role: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
