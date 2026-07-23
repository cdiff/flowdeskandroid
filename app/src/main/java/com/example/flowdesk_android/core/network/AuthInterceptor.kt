package com.example.flowdesk_android.core.network

import com.example.flowdesk_android.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 모든 API 요청에 Authorization: Bearer {accessToken} 헤더를 자동 추가하는 Interceptor.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath
        val token = tokenManager.getToken()

        // 로그인(/auth/login) 및 회원가입(/auth/signup) 경로는 토큰 헤더 추가에서 제외합니다.
        val request = if (token != null && !path.contains("/auth/login") && !path.contains("/auth/signup")) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
