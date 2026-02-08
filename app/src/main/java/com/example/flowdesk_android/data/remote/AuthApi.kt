package com.example.flowdesk_android.data.remote

import com.example.flowdesk_android.data.remote.dto.LoginRequest
import com.example.flowdesk_android.data.remote.dto.LoginResponse
import com.example.flowdesk_android.data.remote.dto.SignUpRequest
import com.example.flowdesk_android.data.remote.dto.SignUpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>
}
