package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.data.remote.dto.RoleDto
import com.example.flowdesk_android.data.remote.dto.CreateRoleRequest

interface RoleRepository {
    suspend fun getRoles(): Result<List<RoleDto>>
    suspend fun createRole(request: CreateRoleRequest): Result<Unit>
}
