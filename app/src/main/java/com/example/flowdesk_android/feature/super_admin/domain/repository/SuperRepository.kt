package com.example.flowdesk_android.feature.super_admin.domain.repository

import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.model.TenantDetail

interface SuperRepository {
    suspend fun getDashboard(): Result<DashboardStats>

    // ── 테넌트 관리 ──────────────────────────────────
    suspend fun getTenants(page: Int = 1, limit: Int = 20, search: String? = null): Result<List<Tenant>>
    suspend fun createTenant(tenantName: String, displayName: String, domain: String): Result<Tenant>
    suspend fun getTenantDetail(tenantId: Int): Result<TenantDetail>
    suspend fun updateTenant(tenantId: Int, tenantName: String?, displayName: String?, domain: String?, isActive: Int?): Result<Tenant>
    suspend fun deleteTenant(tenantId: Int): Result<Unit>
    suspend fun updateTenantStatus(tenantId: Int, isActive: Boolean): Result<Tenant>
}
