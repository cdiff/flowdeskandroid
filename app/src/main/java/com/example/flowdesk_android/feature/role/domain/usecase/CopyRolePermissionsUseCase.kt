package com.example.flowdesk_android.feature.role.domain.usecase

import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import javax.inject.Inject

class CopyRolePermissionsUseCase @Inject constructor(private val repository: RoleRepository) {
    suspend operator fun invoke(targetId: Int, sourceRoleId: Int): Result<Unit> =
        repository.copyRolePermissions(targetId, sourceRoleId)
}
