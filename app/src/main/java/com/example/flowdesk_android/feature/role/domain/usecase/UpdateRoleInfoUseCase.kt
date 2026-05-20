package com.example.flowdesk_android.feature.role.domain.usecase

import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import javax.inject.Inject

class UpdateRoleInfoUseCase @Inject constructor(private val repository: RoleRepository) {
    suspend operator fun invoke(id: Int, roleName: String, displayName: String, description: String?): Result<Unit> =
        repository.updateRoleInfo(id, roleName, displayName, description)
}
