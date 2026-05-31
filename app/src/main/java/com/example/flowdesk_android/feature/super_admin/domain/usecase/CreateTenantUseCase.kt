package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class CreateTenantUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        tenantName: String,
        displayName: String,
        domain: String
    ): Result<Tenant> = repository.createTenant(tenantName, displayName, domain)
}
