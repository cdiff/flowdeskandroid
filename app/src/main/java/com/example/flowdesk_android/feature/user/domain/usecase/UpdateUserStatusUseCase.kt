package com.example.flowdesk_android.feature.user.domain.usecase

import com.example.flowdesk_android.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserStatusUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int, isActive: Boolean): Result<Unit> =
        repository.updateUserStatus(userId, isActive)
}
