package com.example.flowdesk_android.feature.user.domain.usecase

import com.example.flowdesk_android.feature.user.domain.model.User
import com.example.flowdesk_android.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        password: String,
        corpName: String,
        userName: String,
        userEmail: String,
        userTel: String,
        userHp: String,
        roleIds: List<Int>?
    ): Result<User> {
        return repository.createUser(
            userId = userId,
            password = password,
            corpName = corpName,
            userName = userName,
            userEmail = userEmail,
            userTel = userTel,
            userHp = userHp,
            roleIds = roleIds
        )
    }
}
