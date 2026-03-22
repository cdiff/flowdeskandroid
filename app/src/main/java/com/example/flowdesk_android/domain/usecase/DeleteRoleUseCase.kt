package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class DeleteRoleUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return repository.deleteRole(id)
    }
}
