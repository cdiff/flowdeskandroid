package com.example.flowdesk_android.feature.role.data.api

import com.example.flowdesk_android.feature.role.data.dto.CreateRoleRequest
import com.example.flowdesk_android.feature.role.data.dto.CopyRolePermissionsRequest
import com.example.flowdesk_android.feature.role.data.dto.PermissionCatalogResponse
import com.example.flowdesk_android.feature.role.data.dto.RoleDetailResponse
import com.example.flowdesk_android.feature.role.data.dto.RolesResponse
import com.example.flowdesk_android.feature.role.data.dto.UpdateRoleInfoRequest
import com.example.flowdesk_android.feature.role.data.dto.UpdateRolePermissionsRequest
import com.example.flowdesk_android.feature.role.data.dto.UpdateRoleStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RoleApi {

    @GET("/roles")
    suspend fun getRoles(): Response<RolesResponse>

    @GET("/permissions/catalog")
    suspend fun getPermissionCatalog(): Response<PermissionCatalogResponse>

    @POST("/roles")
    suspend fun createRole(@Body request: CreateRoleRequest): Response<Unit>

    @GET("/roles/{id}")
    suspend fun getRoleDetail(@Path("id") id: Int): Response<RoleDetailResponse>

    @PATCH("/roles/{id}/status")
    suspend fun toggleRoleStatus(
        @Path("id") id: Int,
        @Body request: UpdateRoleStatusRequest
    ): Response<Unit>

    @PATCH("/roles/{id}")
    suspend fun updateRoleInfo(
        @Path("id") id: Int,
        @Body request: UpdateRoleInfoRequest
    ): Response<Unit>

    @PATCH("/roles/{id}/permissions")
    suspend fun updateRolePermissions(
        @Path("id") id: Int,
        @Body request: UpdateRolePermissionsRequest
    ): Response<Unit>

    @PUT("/roles/{id}/permissions")
    suspend fun copyRolePermissions(
        @Path("id") id: Int,
        @Body request: CopyRolePermissionsRequest
    ): Response<Unit>

    @DELETE("/roles/{id}")
    suspend fun deleteRole(@Path("id") id: Int): Response<Unit>
}
