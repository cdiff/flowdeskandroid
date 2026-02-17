package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(current: String, new: String, confirm: String): Result<Unit> {
        return repository.changePassword(current, new, confirm)
    }
}
