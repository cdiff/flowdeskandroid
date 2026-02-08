package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(companyName: String, adminName: String, email: String, phone: String, password: String): Result<String> {
        if (companyName.isBlank() || adminName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields must be filled"))
        }
        return repository.signUp(companyName, adminName, email, phone, password)
    }
}
