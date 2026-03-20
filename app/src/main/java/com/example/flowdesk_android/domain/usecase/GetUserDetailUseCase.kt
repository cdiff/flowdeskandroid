package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.UserRepository
import com.example.flowdesk_android.data.remote.dto.UserDetailDto
import javax.inject.Inject

class GetUserDetailUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int): Result<UserDetailDto> = userRepository.getUserDetail(id)
}
