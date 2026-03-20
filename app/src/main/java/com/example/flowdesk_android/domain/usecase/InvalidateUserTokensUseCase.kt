package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.UserRepository
import javax.inject.Inject

class InvalidateUserTokensUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> = userRepository.invalidateUserTokens(id)
}
