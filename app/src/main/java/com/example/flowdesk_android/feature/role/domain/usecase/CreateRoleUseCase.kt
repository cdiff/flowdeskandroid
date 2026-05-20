package com.example.flowdesk_android.feature.role.domain.usecase

import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import javax.inject.Inject

class CreateRoleUseCase @Inject constructor(private val repository: RoleRepository) {
    suspend operator fun invoke(roleName: String, displayName: String, description: String): Result<Unit> =
        repository.createRole(roleName, displayName, description)
}
