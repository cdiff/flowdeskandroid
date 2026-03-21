package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.CreateRoleRequest
import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class CreateRoleUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(request: CreateRoleRequest): Result<Unit> {
        return repository.createRole(request)
    }
}
