package com.example.flowdesk_android.core.network

import com.example.flowdesk_android.data.local.TokenManager
import com.example.flowdesk_android.feature.auth.data.api.AuthRefreshApi
import com.example.flowdesk_android.feature.auth.data.dto.RefreshTokenRequest
import com.example.flowdesk_android.feature.auth.data.repository.AuthRepositoryImpl
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * OkHttp Authenticator — 401 응답 시 자동으로 토큰 갱신을 시도합니다.
 *
 * 동작 흐름:
 * 1. 401 응답 수신
 * 2. refreshToken으로 /auth/refresh 동기 호출
 * 3. 성공 → 새 토큰 저장 후 원래 요청 재시도
 * 4. 실패 → 토큰 삭제 + 세션 Guest 전환 + null 반환 (요청 포기)
 *
 * synchronized 블록으로 동시 다발적 401에 대한 중복 refresh를 방지합니다.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRefreshApi: AuthRefreshApi,
    private val authRepositoryProvider: Provider<AuthRepositoryImpl>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 무한 루프 방지: refresh 요청 자체가 401이면 포기
        if (response.request.url.encodedPath.contains("auth/refresh")) {
            return null
        }

        // 이미 한 번 재시도한 요청이면 포기
        if (responseCount(response) >= 2) {
            authRepositoryProvider.get().clearSessionDueToAuthFailure()
            return null
        }

        synchronized(this) {
            // 다른 스레드가 이미 토큰을 갱신했는지 확인
            val currentToken = tokenManager.getToken()
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            // 현재 저장된 토큰이 실패한 요청의 토큰과 다르면 이미 갱신된 것
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // refreshToken으로 갱신 시도
            val refreshToken = tokenManager.getRefreshToken() ?: run {
                authRepositoryProvider.get().clearSessionDueToAuthFailure()
                return null
            }

            return try {
                val refreshResponse = authRefreshApi
                    .refreshTokenSync(RefreshTokenRequest(refreshToken))
                    .execute()

                if (refreshResponse.isSuccessful) {
                    val body = refreshResponse.body()!!
                    tokenManager.saveToken(body.accessToken)
                    tokenManager.saveRefreshToken(body.refreshToken)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${body.accessToken}")
                        .build()
                } else {
                    // refresh 실패 → 강제 로그아웃
                    authRepositoryProvider.get().clearSessionDueToAuthFailure()
                    null
                }
            } catch (e: Exception) {
                authRepositoryProvider.get().clearSessionDueToAuthFailure()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
