package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.RoleDetailResponse
import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class GetRoleDetailUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(id: Int): Result<RoleDetailResponse> {
        return repository.getRoleDetail(id)
    }
}
