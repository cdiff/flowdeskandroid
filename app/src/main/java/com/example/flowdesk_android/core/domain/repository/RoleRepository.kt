package com.example.flowdesk_android.core.domain.repository

import com.example.flowdesk_android.core.domain.model.PermissionCatalog
import com.example.flowdesk_android.core.domain.model.Role
import com.example.flowdesk_android.core.domain.model.RoleDetail

interface RoleRepository {
    suspend fun getRoles(): Result<List<Role>>
    suspend fun getRoleDetail(id: Int): Result<RoleDetail>
    suspend fun createRole(roleName: String, displayName: String, description: String): Result<Unit>
    suspend fun deleteRole(id: Int): Result<Unit>
    suspend fun toggleRoleStatus(id: Int, isActive: Boolean): Result<Unit>
    suspend fun updateRoleInfo(id: Int, roleName: String, displayName: String, description: String?): Result<Unit>
    suspend fun updateRolePermissions(id: Int, add: List<Int>?, remove: List<Int>?): Result<Unit>
    suspend fun copyRolePermissions(id: Int, sourceRoleId: Int): Result<Unit>
    suspend fun getPermissionCatalog(): Result<PermissionCatalog>
}
