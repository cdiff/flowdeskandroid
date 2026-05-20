package com.example.flowdesk_android.feature.user.domain.usecase

import com.example.flowdesk_android.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserRolesUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int, add: List<Int>?, remove: List<Int>?): Result<Unit> =
        repository.updateUserRoles(userId, add, remove)
}
