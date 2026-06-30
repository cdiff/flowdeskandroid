package com.example.flowdesk_android.feature.auth.data.api

import com.example.flowdesk_android.feature.auth.data.dto.RefreshTokenRequest
import com.example.flowdesk_android.feature.auth.data.dto.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * OkHttp Authenticator는 동기 호출만 가능하므로,
 * refresh 전용 동기 Retrofit 인터페이스를 별도로 정의합니다.
 */
interface AuthRefreshApi {
    @POST("auth/refresh")
    fun refreshTokenSync(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
}
