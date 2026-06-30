package com.example.flowdesk_android.feature.system_management.domain.model

import java.io.Serializable

data class TenantStatus(
    val tenantStatusId: Long,
    val statusGroup: String,
    val statusKey: String,
    val statusName: String,
    val description: String,
    val color: String,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
) : Serializable

data class TenantStatusGroup(
    val statusGroup: String,
    val count: Int,
    val items: List<TenantStatus>
) : Serializable

data class TenantStatusListResponse(
    val groups: List<TenantStatusGroup>,
    val total: Int
) : Serializable
