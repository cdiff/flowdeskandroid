package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.UserDto
import com.example.flowdesk_android.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<List<UserDto>> {
        return repository.getUsers()
    }
}
