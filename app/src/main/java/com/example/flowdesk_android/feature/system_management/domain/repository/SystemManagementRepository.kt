package com.example.flowdesk_android.feature.system_management.domain.repository

import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusListResponse

interface SystemManagementRepository {
    suspend fun getTenantStatuses(
        statusGroup: String?,
        isActive: String?,
        q: String?
    ): Result<TenantStatusListResponse>

    suspend fun createTenantStatus(
        statusGroup: String,
        statusKey: String,
        statusName: String,
        description: String,
        color: String,
        sortOrder: Int,
        isActive: Int
    ): Result<TenantStatus>

    suspend fun getTenantStatusDetail(id: Long): Result<TenantStatus>

    suspend fun updateTenantStatus(
        id: Long,
        statusName: String,
        description: String,
        color: String,
        sortOrder: Int,
        isActive: Int
    ): Result<TenantStatus>

    suspend fun deleteTenantStatus(id: Long): Result<Unit>

    suspend fun updateTenantStatusActive(id: Long, isActive: Int): Result<TenantStatus>
}
