package com.example.flowdesk_android.feature.super_admin.data.dto

import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.model.TenantDetail
import com.google.gson.annotations.SerializedName

// ──────────────────────────────────────────────────
// Request DTOs
// ──────────────────────────────────────────────────

data class CreateTenantRequest(
    @SerializedName("tenantName") val tenantName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("isActive") val isActive: Int = 1
)

data class UpdateTenantRequest(
    @SerializedName("tenantName") val tenantName: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("isActive") val isActive: Int? = null
)

data class UpdateTenantStatusRequest(
    @SerializedName("isActive") val isActive: Int   // 1: 활성, 0: 비활성
)

// ──────────────────────────────────────────────────
// Response DTOs
// ──────────────────────────────────────────────────

data class TenantDto(
    @SerializedName("tenantId") val tenantId: Int = 0,
    @SerializedName("tenantName") val tenantName: String = "",
    @SerializedName("displayName") val displayName: String = "",
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("isActive") val isActive: Int = 1,
    @SerializedName("userCount") val userCount: Int = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain() = Tenant(
        tenantId = tenantId,
        tenantName = tenantName,
        displayName = displayName,
        domain = domain,
        isActive = isActive == 1,
        userCount = userCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class TenantDetailDto(
    @SerializedName("tenantId") val tenantId: Int = 0,
    @SerializedName("tenantName") val tenantName: String = "",
    @SerializedName("displayName") val displayName: String = "",
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("isActive") val isActive: Int = 1,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain() = TenantDetail(
        tenantId = tenantId,
        tenantName = tenantName,
        displayName = displayName,
        domain = domain,
        isActive = isActive == 1,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class TenantsResponse(
    @SerializedName("items") val items: List<TenantDto>?,
    @SerializedName("pageInfo") val pageInfo: TenantPageInfoDto?
)

data class TenantPageInfoDto(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPages") val totalPages: Int
)
