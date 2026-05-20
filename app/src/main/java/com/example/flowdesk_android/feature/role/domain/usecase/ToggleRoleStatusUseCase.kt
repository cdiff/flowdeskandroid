package com.example.flowdesk_android.feature.role.domain.usecase

import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import javax.inject.Inject

class ToggleRoleStatusUseCase @Inject constructor(private val repository: RoleRepository) {
    suspend operator fun invoke(roleId: Int, isActive: Boolean): Result<Unit> =
        repository.toggleRoleStatus(roleId, isActive)
}
