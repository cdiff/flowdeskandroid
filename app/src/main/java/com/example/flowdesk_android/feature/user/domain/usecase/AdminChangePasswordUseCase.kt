package com.example.flowdesk_android.feature.user.domain.usecase

import com.example.flowdesk_android.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class AdminChangePasswordUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int, newPassword: String): Result<Unit> =
        repository.adminChangePassword(userId, newPassword)
}
