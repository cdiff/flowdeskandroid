package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class ToggleRoleStatusUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(id: Int, isActive: Int): Result<Unit> {
        return repository.toggleRoleStatus(id, isActive)
    }
}
