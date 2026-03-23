package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.data.remote.dto.RoleDto
import com.example.flowdesk_android.data.remote.dto.CreateRoleRequest

interface RoleRepository {
    suspend fun getRoles(): Result<List<RoleDto>>
    suspend fun createRole(request: CreateRoleRequest): Result<Unit>
    suspend fun getRoleDetail(id: Int): Result<com.example.flowdesk_android.data.remote.dto.RoleDetailResponse>
    suspend fun toggleRoleStatus(id: Int, isActive: Int): Result<Unit>
    suspend fun deleteRole(id: Int): Result<Unit>
    suspend fun updateRoleInfo(id: Int, request: com.example.flowdesk_android.data.remote.dto.UpdateRoleInfoRequest): Result<Unit>
    suspend fun updateRolePermissions(id: Int, request: com.example.flowdesk_android.data.remote.dto.UpdateRolePermissionsRequest): Result<Unit>
    suspend fun copyRolePermissions(id: Int, request: com.example.flowdesk_android.data.remote.dto.CopyRolePermissionsRequest): Result<Unit>
    suspend fun getPermissionCatalog(): Result<com.example.flowdesk_android.data.remote.dto.PermissionCatalogResponse>
}
