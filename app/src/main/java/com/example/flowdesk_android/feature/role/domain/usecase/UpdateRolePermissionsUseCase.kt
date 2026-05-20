package com.example.flowdesk_android.feature.role.domain.usecase

import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import javax.inject.Inject

class UpdateRolePermissionsUseCase @Inject constructor(private val repository: RoleRepository) {
    suspend operator fun invoke(id: Int, add: List<Int>?, remove: List<Int>?): Result<Unit> =
        repository.updateRolePermissions(id, add, remove)
}
