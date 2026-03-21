package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.RoleDto
import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class GetRolesUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(): Result<List<RoleDto>> {
        return repository.getRoles()
    }
}
