package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.UpdateUserInfoRequest
import com.example.flowdesk_android.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int, request: UpdateUserInfoRequest): Result<Unit> = userRepository.updateUser(id, request)
}
