package com.example.flowdesk_android.feature.user.domain.usecase

import com.example.flowdesk_android.feature.user.domain.model.User
import com.example.flowdesk_android.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<List<User>> = repository.getUsers()
}
