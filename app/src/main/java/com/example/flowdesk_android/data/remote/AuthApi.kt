package com.example.flowdesk_android.data.remote

import com.example.flowdesk_android.data.remote.dto.LoginRequest
import com.example.flowdesk_android.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
