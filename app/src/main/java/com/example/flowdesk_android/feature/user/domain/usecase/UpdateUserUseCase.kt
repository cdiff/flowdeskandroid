package com.example.flowdesk_android.feature.user.domain.usecase

import com.example.flowdesk_android.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        id: Int,
        corpName: String?,
        userName: String?,
        userEmail: String?,
        userTel: String?,
        userHp: String?
    ): Result<Unit> = repository.updateUser(id, corpName, userName, userEmail, userTel, userHp)
}
