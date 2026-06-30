package com.example.flowdesk_android.feature.auth.domain.usecase

import com.example.flowdesk_android.feature.auth.domain.model.*
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthenticateSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    val sessionState: StateFlow<AuthSession> = repository.sessionState

    suspend fun login(command: LoginCommand): Result<Unit> {
        return repository.login(command)
    }

    suspend fun initializeSession() {
        repository.initializeSession()
    }

    suspend fun logout(): Result<Unit> {
        return repository.logout()
    }
}
