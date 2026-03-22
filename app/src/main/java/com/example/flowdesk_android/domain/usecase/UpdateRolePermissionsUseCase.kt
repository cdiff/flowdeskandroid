package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.UpdateRolePermissionsRequest
import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class UpdateRolePermissionsUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(id: Int, request: UpdateRolePermissionsRequest): Result<Unit> {
        return repository.updateRolePermissions(id, request)
    }
}
