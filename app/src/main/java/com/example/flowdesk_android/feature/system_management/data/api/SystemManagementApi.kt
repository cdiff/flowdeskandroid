package com.example.flowdesk_android.feature.system_management.data.api

import com.example.flowdesk_android.feature.system_management.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SystemManagementApi {
    @GET("tenants/status")
    suspend fun getTenantStatuses(
        @Query("statusGroup") statusGroup: String?,
        @Query("isActive") isActive: String?,
        @Query("q") q: String?
    ): Response<TenantStatusListResponseDto>

    @POST("tenants/status")
    suspend fun createTenantStatus(
        @Body request: CreateTenantStatusRequest
    ): Response<TenantStatusDto>

    @GET("tenants/status/{id}")
    suspend fun getTenantStatusDetail(
        @Path("id") id: Long
    ): Response<TenantStatusDto>

    @PATCH("tenants/status/{id}")
    suspend fun updateTenantStatus(
        @Path("id") id: Long,
        @Body request: UpdateTenantStatusRequest
    ): Response<TenantStatusDto>

    @DELETE("tenants/status/{id}")
    suspend fun deleteTenantStatus(
        @Path("id") id: Long
    ): Response<Unit>

    @PATCH("tenants/status/{id}/status")
    suspend fun updateTenantStatusActive(
        @Path("id") id: Long,
        @Body request: UpdateActiveRequest
    ): Response<TenantStatusDto>
}
