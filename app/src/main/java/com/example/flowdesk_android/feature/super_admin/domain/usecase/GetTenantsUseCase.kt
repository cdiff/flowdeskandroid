package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class GetTenantsUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null
    ): Result<List<Tenant>> = repository.getTenants(page, limit, search)
}
