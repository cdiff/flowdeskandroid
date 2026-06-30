package com.example.flowdesk_android.feature.user_management.data.api

import com.example.flowdesk_android.feature.user_management.data.dto.AdminChangePasswordRequest
import com.example.flowdesk_android.feature.user_management.data.dto.CreateUserRequest
import com.example.flowdesk_android.feature.user_management.data.dto.UpdateUserInfoRequest
import com.example.flowdesk_android.feature.user_management.data.dto.UpdateUserRolesRequest
import com.example.flowdesk_android.feature.user_management.data.dto.UpdateUserStatusRequest
import com.example.flowdesk_android.feature.user_management.data.dto.UserDetailDto
import com.example.flowdesk_android.feature.user_management.data.dto.UserDto
import com.example.flowdesk_android.feature.user_management.data.dto.UsersResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @GET("users")
    suspend fun getUsers(): Response<UsersResponse>

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserDto>

    @GET("users/{id}")
    suspend fun getUserDetail(@Path("id") id: Int): Response<UserDetailDto>

    @PATCH("users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: Int,
        @Body request: UpdateUserStatusRequest
    ): Response<Unit>

    @PATCH("/users/{id}/roles")
    suspend fun updateUserRoles(
        @Path("id") id: Int,
        @Body request: UpdateUserRolesRequest
    ): Response<Unit>

    @PATCH("users/{id}/password")
    suspend fun adminChangePassword(
        @Path("id") id: Int,
        @Body request: AdminChangePasswordRequest
    ): Response<Unit>

    @POST("users/{id}/invalidate-tokens")
    suspend fun invalidateUserTokens(@Path("id") id: Int): Response<Unit>

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UpdateUserInfoRequest
    ): Response<Unit>
}
