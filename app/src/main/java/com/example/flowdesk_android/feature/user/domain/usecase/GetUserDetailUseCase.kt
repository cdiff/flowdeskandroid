package com.example.flowdesk_android.feature.user.domain.usecase

import com.example.flowdesk_android.feature.user.domain.model.UserDetail
import com.example.flowdesk_android.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class GetUserDetailUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int): Result<UserDetail> = repository.getUserDetail(userId)
}
