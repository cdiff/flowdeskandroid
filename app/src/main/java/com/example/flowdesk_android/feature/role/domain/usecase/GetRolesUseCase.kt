package com.example.flowdesk_android.feature.role.domain.usecase

import com.example.flowdesk_android.feature.role.domain.model.Role
import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import javax.inject.Inject

class GetRolesUseCase @Inject constructor(private val repository: RoleRepository) {
    suspend operator fun invoke(): Result<List<Role>> = repository.getRoles()
}
