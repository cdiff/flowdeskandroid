package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.UpdateRoleInfoRequest
import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class UpdateRoleInfoUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(id: Int, request: UpdateRoleInfoRequest): Result<Unit> {
        return repository.updateRoleInfo(id, request)
    }
}
