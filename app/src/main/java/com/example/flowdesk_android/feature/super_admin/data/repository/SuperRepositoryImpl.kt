package com.example.flowdesk_android.feature.super_admin.data.repository

import com.example.flowdesk_android.feature.super_admin.data.api.SuperApi
import com.example.flowdesk_android.feature.super_admin.data.dto.CreatePageRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.CreateTenantRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdatePageRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdateTenantRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdateTenantStatusRequest
import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats
import com.example.flowdesk_android.feature.super_admin.domain.model.Page
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.model.TenantDetail
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuperRepositoryImpl @Inject constructor(
    private val api: SuperApi
) : SuperRepository {

    override suspend fun getDashboard(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getDashboard()
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("대시보드 조회 실패 (${response.code()})")
            }
        }
    }

    // ── 테넌트 관리 ──────────────────────────────────────────

    override suspend fun getTenants(
        page: Int,
        limit: Int,
        search: String?
    ): Result<List<Tenant>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getTenants(page, limit, search)
            if (response.isSuccessful) {
                response.body()?.items?.map { it.toDomain() } ?: emptyList()
            } else {
                throw Exception("테넌트 목록 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun createTenant(
        tenantName: String,
        displayName: String,
        domain: String
    ): Result<Tenant> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.createTenant(CreateTenantRequest(tenantName, displayName, domain))
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("테넌트 생성 실패 (${response.code()})")
            }
        }
    }

    override suspend fun getTenantDetail(tenantId: Int): Result<TenantDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getTenantDetail(tenantId)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("테넌트 상세 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun updateTenant(
        tenantId: Int,
        tenantName: String?,
        displayName: String?,
        domain: String?,
        isActive: Int?
    ): Result<Tenant> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateTenant(tenantId, UpdateTenantRequest(tenantName, displayName, domain, isActive))
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("테넌트 수정 실패 (${response.code()})")
            }
        }
    }

    override suspend fun deleteTenant(tenantId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.deleteTenant(tenantId)
            if (!response.isSuccessful) throw Exception("테넌트 삭제 실패 (${response.code()})")
        }
    }

    override suspend fun updateTenantStatus(tenantId: Int, isActive: Boolean): Result<Tenant> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateTenantStatus(tenantId, UpdateTenantStatusRequest(if (isActive) 1 else 0))
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상태 변경 실패 (${response.code()})")
            }
        }
    }

    // ── 페이지 관리 ───────────────────────────────────────

    override suspend fun getPages(
        page: Int,
        limit: Int,
        search: String?
    ): Result<List<Page>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getPages(page, limit, search)
            if (response.isSuccessful) {
                response.body()?.items?.map { it.toDomain() } ?: emptyList()
            } else {
                throw Exception("페이지 목록 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun createPage(
        pageName: String,
        path: String,
        displayName: String,
        description: String?,
        parentId: Int?,
        sortOrder: Int
    ): Result<Page> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.createPage(CreatePageRequest(pageName, path, displayName, description, parentId, sortOrder))
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("페이지 생성 실패 (${response.code()})")
            }
        }
    }

    override suspend fun getPageDetail(pageId: Int): Result<Page> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getPageDetail(pageId)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("페이지 상세 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun updatePage(
        pageId: Int,
        pageName: String?,
        path: String?,
        displayName: String?,
        description: String?,
        parentId: Int?,
        sortOrder: Int?,
        isActive: Int?
    ): Result<Page> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updatePage(pageId, UpdatePageRequest(pageName, path, displayName, description, parentId, sortOrder, isActive))
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("페이지 수정 실패 (${response.code()})")
            }
        }
    }

    override suspend fun deletePage(pageId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.deletePage(pageId)
            if (!response.isSuccessful) throw Exception("페이지 삭제 실패 (${response.code()})")
        }
    }
}
