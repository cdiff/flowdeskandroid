package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.UserDto
import com.example.flowdesk_android.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        userName: String,
        userEmail: String,
        userTel: String?,
        userHp: String?
    ): Result<UserDto> {
        return repository.updateProfile(userName, userEmail, userTel, userHp)
    }
}
