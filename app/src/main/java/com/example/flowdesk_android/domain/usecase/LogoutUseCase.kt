package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(allDevices: Boolean = false): Result<Unit> {
        return if (allDevices) {
            repository.logoutAll()
        } else {
            repository.logout()
        }
    }
}
