package com.example.flowdesk_android.feature.role.data.repository

import com.example.flowdesk_android.feature.role.data.api.RoleApi
import com.example.flowdesk_android.feature.role.data.dto.CopyRolePermissionsRequest
import com.example.flowdesk_android.feature.role.data.dto.CreateRoleRequest
import com.example.flowdesk_android.feature.role.data.dto.UpdateRoleInfoRequest
import com.example.flowdesk_android.feature.role.data.dto.UpdateRolePermissionsRequest
import com.example.flowdesk_android.feature.role.data.dto.UpdateRoleStatusRequest
import com.example.flowdesk_android.feature.role.domain.model.PermissionCatalog
import com.example.flowdesk_android.feature.role.domain.model.Role
import com.example.flowdesk_android.feature.role.domain.model.RoleDetail
import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRepositoryImpl @Inject constructor(
    private val api: RoleApi
) : RoleRepository {

    override suspend fun getRoles(): Result<List<Role>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getRoles()
            if (response.isSuccessful) response.body()?.items?.map { it.toDomain() } ?: emptyList()
            else throw Exception("역할 목록 조회 실패 (${response.code()})")
        }
    }

    override suspend fun getRoleDetail(id: Int): Result<RoleDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getRoleDetail(id)
            if (response.isSuccessful) response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            else throw Exception("역할 상세 조회 실패 (${response.code()})")
        }
    }

    override suspend fun createRole(roleName: String, displayName: String, description: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.createRole(CreateRoleRequest(roleName, displayName, description))
            if (!response.isSuccessful) throw Exception("역할 생성 실패 (${response.code()})")
        }
    }

    override suspend fun deleteRole(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.deleteRole(id)
            if (!response.isSuccessful) {
                val msg = if (response.code() in listOf(400, 409)) "사용 중인 역할은 삭제할 수 없습니다."
                          else "역할 삭제 실패 (${response.code()})"
                throw Exception(msg)
            }
        }
    }

    override suspend fun toggleRoleStatus(id: Int, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.toggleRoleStatus(id, UpdateRoleStatusRequest(if (isActive) 1 else 0))
            if (!response.isSuccessful) throw Exception("상태 변경 실패 (${response.code()})")
        }
    }

    override suspend fun updateRoleInfo(id: Int, roleName: String, displayName: String, description: String?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateRoleInfo(id, UpdateRoleInfoRequest(roleName, displayName, description))
            if (!response.isSuccessful) throw Exception("역할 정보 수정 실패 (${response.code()})")
        }
    }

    override suspend fun updateRolePermissions(id: Int, add: List<Int>?, remove: List<Int>?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateRolePermissions(id, UpdateRolePermissionsRequest(add, remove))
            if (!response.isSuccessful) throw Exception("권한 수정 실패 (${response.code()})")
        }
    }

    override suspend fun copyRolePermissions(id: Int, sourceRoleId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.copyRolePermissions(id, CopyRolePermissionsRequest(sourceRoleId))
            if (!response.isSuccessful) throw Exception("권한 복사 실패 (${response.code()})")
        }
    }

    override suspend fun getPermissionCatalog(): Result<PermissionCatalog> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getPermissionCatalog()
            if (response.isSuccessful) response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            else throw Exception("권한 카탈로그 조회 실패 (${response.code()})")
        }
    }
}
