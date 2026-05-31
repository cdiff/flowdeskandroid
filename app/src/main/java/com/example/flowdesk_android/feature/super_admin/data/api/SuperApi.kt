package com.example.flowdesk_android.feature.super_admin.data.api

import com.example.flowdesk_android.feature.super_admin.data.dto.CreateTenantRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.SuperDashboardResponse
import com.example.flowdesk_android.feature.super_admin.data.dto.TenantDetailDto
import com.example.flowdesk_android.feature.super_admin.data.dto.TenantDto
import com.example.flowdesk_android.feature.super_admin.data.dto.TenantsResponse
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdateTenantRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdateTenantStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SuperApi {

    @GET("super/dashboard")
    suspend fun getDashboard(): Response<SuperDashboardResponse>

    // ── 테넌트 관리 ──────────────────────────────────

    @GET("tenants")
    suspend fun getTenants(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<TenantsResponse>

    @POST("tenants")
    suspend fun createTenant(@Body request: CreateTenantRequest): Response<TenantDto>

    @GET("tenants/{id}")
    suspend fun getTenantDetail(@Path("id") id: Int): Response<TenantDetailDto>

    @PATCH("tenants/{id}")
    suspend fun updateTenant(
        @Path("id") id: Int,
        @Body request: UpdateTenantRequest
    ): Response<TenantDto>

    @DELETE("tenants/{id}")
    suspend fun deleteTenant(@Path("id") id: Int): Response<Unit>

    @PATCH("tenants/{id}/status")
    suspend fun updateTenantStatus(
        @Path("id") id: Int,
        @Body request: UpdateTenantStatusRequest
    ): Response<TenantDto>
}
