package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserRolesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int, roleIds: List<Int>): Result<Unit> = userRepository.updateUserRoles(id, roleIds)
}
