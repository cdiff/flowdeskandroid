package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.SuperDashboardResponse
import com.example.flowdesk_android.domain.repository.SuperRepository
import javax.inject.Inject

class GetSuperDashboardUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(): Result<SuperDashboardResponse> {
        return repository.getDashboard()
    }
}
