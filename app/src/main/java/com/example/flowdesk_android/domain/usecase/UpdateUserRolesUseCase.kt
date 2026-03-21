package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.UserRepository
import com.example.flowdesk_android.data.remote.dto.UpdateUserRolesRequest
import javax.inject.Inject

class UpdateUserRolesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int, request: UpdateUserRolesRequest): Result<Unit> = userRepository.updateUserRoles(id, request)
}
