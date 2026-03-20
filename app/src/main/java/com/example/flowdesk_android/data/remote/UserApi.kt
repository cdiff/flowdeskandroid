package com.example.flowdesk_android.data.remote

import com.example.flowdesk_android.data.remote.dto.UsersResponse
import retrofit2.Response
import retrofit2.http.GET

import com.example.flowdesk_android.data.remote.dto.*
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body

interface UserApi {
    @GET("users")
    suspend fun getUsers(): Response<UsersResponse>

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserDto>

    @GET("users/{id}")
    suspend fun getUserDetail(@Path("id") id: Int): Response<UserDetailDto>

    @PATCH("users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: Int, @Body request: UpdateUserStatusRequest): Response<Unit>

    @PATCH("users/{id}/roles")
    suspend fun updateUserRoles(@Path("id") id: Int, @Body request: UpdateUserRolesRequest): Response<Unit>

    @PATCH("users/{id}/password")
    suspend fun adminChangePassword(@Path("id") id: Int, @Body request: AdminChangePasswordRequest): Response<Unit>

    @POST("users/{id}/invalidate-tokens")
    suspend fun invalidateUserTokens(@Path("id") id: Int): Response<Unit>

    @PATCH("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body request: UpdateUserInfoRequest): Response<Unit>
}
