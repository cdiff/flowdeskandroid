package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class UpdateTenantStatusUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(tenantId: Int, isActive: Boolean): Result<Tenant> =
        repository.updateTenantStatus(tenantId, isActive)
}
