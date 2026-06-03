package com.example.flowdesk_android.feature.super_admin.data.api

import com.example.flowdesk_android.feature.super_admin.data.dto.ActionDto
import com.example.flowdesk_android.feature.super_admin.data.dto.ActionsResponse
import com.example.flowdesk_android.feature.super_admin.data.dto.CreateActionRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.CreatePageRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.CreateTenantRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.PageDto
import com.example.flowdesk_android.feature.super_admin.data.dto.PagesResponse
import com.example.flowdesk_android.feature.super_admin.data.dto.SuperDashboardResponse
import com.example.flowdesk_android.feature.super_admin.data.dto.TenantDetailDto
import com.example.flowdesk_android.feature.super_admin.data.dto.TenantDto
import com.example.flowdesk_android.feature.super_admin.data.dto.TenantsResponse
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdateActionRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdateActionStatusRequest
import com.example.flowdesk_android.feature.super_admin.data.dto.UpdatePageRequest
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

    // ── 페이지 관리 ──────────────────────────────────────

    @GET("permissions/admin/pages")
    suspend fun getPages(
        @Query("page")   page: Int = 1,
        @Query("limit")  limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<PagesResponse>

    @POST("permissions/admin/pages")
    suspend fun createPage(@Body request: CreatePageRequest): Response<PageDto>

    @GET("permissions/admin/pages/{id}")
    suspend fun getPageDetail(@Path("id") id: Int): Response<PageDto>

    @PATCH("permissions/admin/pages/{id}")
    suspend fun updatePage(
        @Path("id") id: Int,
        @Body request: UpdatePageRequest
    ): Response<PageDto>

    @DELETE("permissions/admin/pages/{id}")
    suspend fun deletePage(@Path("id") id: Int): Response<Unit>

    // ── 액션 관리 ──────────────────────────────────────────

    @GET("permissions/admin/actions")
    suspend fun getActions(
        @Query("page")   page: Int = 1,
        @Query("limit")  limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<ActionsResponse>

    @POST("permissions/admin/actions")
    suspend fun createAction(@Body request: CreateActionRequest): Response<ActionDto>

    @GET("permissions/admin/actions/{id}")
    suspend fun getActionDetail(@Path("id") id: Int): Response<ActionDto>

    @PATCH("permissions/admin/actions/{id}")
    suspend fun updateAction(
        @Path("id") id: Int,
        @Body request: UpdateActionRequest
    ): Response<ActionDto>

    @DELETE("permissions/admin/actions/{id}")
    suspend fun deleteAction(@Path("id") id: Int): Response<Unit>

    @PATCH("permissions/admin/actions/{id}/status")
    suspend fun updateActionStatus(
        @Path("id") id: Int,
        @Body request: UpdateActionStatusRequest
    ): Response<ActionDto>
}
