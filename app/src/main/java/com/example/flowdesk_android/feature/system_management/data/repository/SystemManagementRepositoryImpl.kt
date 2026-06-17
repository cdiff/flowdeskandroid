package com.example.flowdesk_android.feature.system_management.data.repository

import com.example.flowdesk_android.feature.system_management.data.model.*
import com.example.flowdesk_android.feature.system_management.data.api.SystemManagementApi
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusListResponse
import com.example.flowdesk_android.feature.system_management.domain.repository.SystemManagementRepository
import javax.inject.Inject

class SystemManagementRepositoryImpl @Inject constructor(
    private val apiService: SystemManagementApi
) : SystemManagementRepository {

    override suspend fun getTenantStatuses(
        statusGroup: String?,
        isActive: String?,
        q: String?
    ): Result<TenantStatusListResponse> = runCatching {
        val response = apiService.getTenantStatuses(statusGroup, isActive, q)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createTenantStatus(
        statusGroup: String,
        statusKey: String,
        statusName: String,
        description: String,
        color: String,
        sortOrder: Int,
        isActive: Int
    ): Result<TenantStatus> = runCatching {
        val request = CreateTenantStatusRequest(
            statusGroup = statusGroup,
            statusKey = statusKey,
            statusName = statusName,
            description = description,
            color = color,
            sortOrder = sortOrder,
            isActive = isActive
        )
        val response = apiService.createTenantStatus(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun getTenantStatusDetail(id: Long): Result<TenantStatus> = runCatching {
        val response = apiService.getTenantStatusDetail(id)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun updateTenantStatus(
        id: Long,
        statusName: String,
        description: String,
        color: String,
        sortOrder: Int,
        isActive: Int
    ): Result<TenantStatus> = runCatching {
        val request = UpdateTenantStatusRequest(
            statusName = statusName,
            description = description,
            color = color,
            sortOrder = sortOrder,
            isActive = isActive
        )
        val response = apiService.updateTenantStatus(id, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun deleteTenantStatus(id: Long): Result<Unit> = runCatching {
        val response = apiService.deleteTenantStatus(id)
        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun updateTenantStatusActive(id: Long, isActive: Int): Result<TenantStatus> = runCatching {
        val request = UpdateActiveRequest(isActive = isActive)
        val response = apiService.updateTenantStatusActive(id, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }
}
