package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.CopyRolePermissionsRequest
import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class CopyRolePermissionsUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(id: Int, sourceRoleId: Int): Result<Unit> {
        val request = CopyRolePermissionsRequest(sourceRoleId)
        return repository.copyRolePermissions(id, request)
    }
}
