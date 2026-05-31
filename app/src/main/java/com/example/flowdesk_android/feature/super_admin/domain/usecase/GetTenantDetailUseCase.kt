package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.TenantDetail
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class GetTenantDetailUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(tenantId: Int): Result<TenantDetail> =
        repository.getTenantDetail(tenantId)
}
