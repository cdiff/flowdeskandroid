package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.UserRepository
import javax.inject.Inject

class AdminChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int, newPassword: String): Result<Unit> = userRepository.adminChangePassword(id, newPassword)
}
