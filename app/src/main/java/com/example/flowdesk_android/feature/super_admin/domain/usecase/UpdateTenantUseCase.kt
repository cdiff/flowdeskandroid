package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class UpdateTenantUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        tenantId: Int,
        tenantName: String? = null,
        displayName: String? = null,
        domain: String? = null,
        isActive: Int? = null
    ): Result<Tenant> = repository.updateTenant(tenantId, tenantName, displayName, domain, isActive)
}
