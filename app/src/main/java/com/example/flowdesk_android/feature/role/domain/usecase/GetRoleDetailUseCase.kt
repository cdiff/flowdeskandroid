package com.example.flowdesk_android.feature.role.domain.usecase

import com.example.flowdesk_android.feature.role.domain.model.RoleDetail
import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import javax.inject.Inject

class GetRoleDetailUseCase @Inject constructor(private val repository: RoleRepository) {
    suspend operator fun invoke(roleId: Int): Result<RoleDetail> = repository.getRoleDetail(roleId)
}
