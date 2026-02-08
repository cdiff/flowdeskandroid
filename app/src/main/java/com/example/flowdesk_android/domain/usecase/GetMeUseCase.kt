package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.AuthMeResponse
import com.example.flowdesk_android.domain.repository.AuthRepository
import javax.inject.Inject

class GetMeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<AuthMeResponse> {
        return repository.getMe()
    }
}
