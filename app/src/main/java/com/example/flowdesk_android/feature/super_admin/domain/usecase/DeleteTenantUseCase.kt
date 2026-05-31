package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class DeleteTenantUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(tenantId: Int): Result<Unit> =
        repository.deleteTenant(tenantId)
}
