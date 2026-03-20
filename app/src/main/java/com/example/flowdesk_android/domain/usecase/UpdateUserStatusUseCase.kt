package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int, isActive: Int): Result<Unit> = userRepository.updateUserStatus(id, isActive)
}
