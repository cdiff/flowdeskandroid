package com.example.flowdesk_android.data.remote

import com.example.flowdesk_android.data.remote.dto.CreateRoleRequest
import com.example.flowdesk_android.data.remote.dto.RolesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RoleApi {
    @GET("/roles")
    suspend fun getRoles(): Response<RolesResponse>

    @POST("/roles")
    suspend fun createRole(@Body request: CreateRoleRequest): Response<Unit>
}
