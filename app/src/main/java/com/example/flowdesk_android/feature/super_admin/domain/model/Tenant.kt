package com.example.flowdesk_android.feature.super_admin.domain.model

data class Tenant(
    val tenantId: Int,
    val tenantName: String,
    val displayName: String,
    val domain: String?,
    val isActive: Boolean,
    val userCount: Int,
    val createdAt: String?,
    val updatedAt: String?
)

data class TenantDetail(
    val tenantId: Int,
    val tenantName: String,
    val displayName: String,
    val domain: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
