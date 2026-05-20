package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class GetSuperDashboardUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(): Result<DashboardStats> = repository.getDashboard()
}
