package com.example.flowdesk_android.feature.auth.domain.usecase

import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    private val tenantNamePattern = Regex("^[a-z0-9][a-z0-9-]{2,29}$")
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(
        tenantName: String,
        companyName: String,
        adminName: String,
        email: String,
        phone: String,
        password: String
    ): Result<String> {
        if (tenantName.isBlank() || companyName.isBlank() || adminName.isBlank() ||
            email.isBlank() || phone.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("모든 필드를 입력해야 합니다."))
        }
        if (!tenantNamePattern.matches(tenantName)) {
            return Result.failure(IllegalArgumentException(
                "테넌트 식별자는 영문 소문자, 숫자, 하이픈(-)만 사용 가능하며 3~30자여야 합니다."
            ))
        }
        if (!emailPattern.matches(email)) {
            return Result.failure(IllegalArgumentException("올바른 이메일 형식이 아닙니다."))
        }
        return repository.signUp(tenantName, companyName, adminName, email, phone, password)
    }
}

