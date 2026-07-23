package com.example.flowdesk_android.feature.system_management.data.dto

import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusGroup
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusListResponse
import com.google.gson.annotations.SerializedName

data class TenantStatusDto(
    @SerializedName("tenantStatusId") val tenantStatusId: Long,
    @SerializedName("statusGroup") val statusGroup: String,
    @SerializedName("statusKey") val statusKey: String,
    @SerializedName("statusName") val statusName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("color") val color: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toDomain(): TenantStatus = TenantStatus(
        tenantStatusId = tenantStatusId,
        statusGroup = statusGroup,
        statusKey = statusKey,
        statusName = statusName,
        description = description ?: "",
        color = color,
        sortOrder = sortOrder,
        isActive = isActive == 1,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class TenantStatusGroupDto(
    @SerializedName("statusGroup") val statusGroup: String,
    @SerializedName("count") val count: Int,
    @SerializedName("items") val items: List<TenantStatusDto>
) {
    fun toDomain(): TenantStatusGroup = TenantStatusGroup(
        statusGroup = statusGroup,
        count = count,
        items = items.map { it.toDomain() }
    )
}

data class TenantStatusListResponseDto(
    @SerializedName("groups") val groups: List<TenantStatusGroupDto>,
    @SerializedName("total") val total: Int
) {
    fun toDomain(): TenantStatusListResponse = TenantStatusListResponse(
        groups = groups.map { it.toDomain() },
        total = total
    )
}

data class CreateTenantStatusRequest(
    @SerializedName("statusGroup") val statusGroup: String,
    @SerializedName("statusKey") val statusKey: String,
    @SerializedName("statusName") val statusName: String,
    @SerializedName("description") val description: String,
    @SerializedName("color") val color: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("isActive") val isActive: Int
)

data class UpdateTenantStatusRequest(
    @SerializedName("statusName") val statusName: String,
    @SerializedName("description") val description: String,
    @SerializedName("color") val color: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("isActive") val isActive: Int
)

data class UpdateActiveRequest(
    @SerializedName("isActive") val isActive: Int
)
