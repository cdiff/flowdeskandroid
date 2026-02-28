package com.example.flowdesk_android.data.remote

import com.example.flowdesk_android.data.remote.dto.AuthMeResponse
import com.example.flowdesk_android.data.remote.dto.ChangePasswordRequest
import com.example.flowdesk_android.data.remote.dto.LoginRequest
import com.example.flowdesk_android.data.remote.dto.LoginResponse
import com.example.flowdesk_android.data.remote.dto.LogoutRequest
import com.example.flowdesk_android.data.remote.dto.ProfileUpdateRequest
import com.example.flowdesk_android.data.remote.dto.SignUpRequest
import com.example.flowdesk_android.data.remote.dto.SignUpResponse
import com.example.flowdesk_android.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>

    @POST("auth/logout-all")
    suspend fun logoutAll(@Body request: LogoutRequest): Response<Unit>

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<AuthMeResponse>

    @PATCH("auth/me/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<UserDto>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>
}
