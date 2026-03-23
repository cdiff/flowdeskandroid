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

    override suspend fun getRoleDetail(id: Int): Result<com.example.flowdesk_android.data.remote.dto.RoleDetailResponse> {
        return try {
            val response = api.getRoleDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch role detail: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleRoleStatus(id: Int, isActive: Int): Result<Unit> {
        return try {
            val request = com.example.flowdesk_android.data.remote.dto.UpdateRoleStatusRequest(isActive)
            val response = api.toggleRoleStatus(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("상태 변경 실패 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRole(id: Int): Result<Unit> {
        return try {
            val response = api.deleteRole(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                if (response.code() == 400 || response.code() == 409) {
                    Result.failure(Exception("사용 중인 역할은 삭제할 수 없습니다."))
                } else {
                    Result.failure(Exception("역할 삭제 실패 (${response.code()})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun updateRoleInfo(id: Int, request: com.example.flowdesk_android.data.remote.dto.UpdateRoleInfoRequest): Result<Unit> {
        return try {
            val response = api.updateRoleInfo(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("역할 정보 수정 실패 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRolePermissions(id: Int, request: com.example.flowdesk_android.data.remote.dto.UpdateRolePermissionsRequest): Result<Unit> {
        return try {
            val response = api.updateRolePermissions(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("권한 수정 실패 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun copyRolePermissions(id: Int, request: com.example.flowdesk_android.data.remote.dto.CopyRolePermissionsRequest): Result<Unit> {
        return try {
            val response = api.copyRolePermissions(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("권한 복사 실패 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPermissionCatalog(): Result<com.example.flowdesk_android.data.remote.dto.PermissionCatalogResponse> {
        return try {
            val response = api.getPermissionCatalog()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("권한 카탈로그 조회 실패 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
