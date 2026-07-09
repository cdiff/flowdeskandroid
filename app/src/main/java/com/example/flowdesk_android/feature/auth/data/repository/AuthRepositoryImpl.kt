package com.example.flowdesk_android.feature.auth.data.repository

import com.example.flowdesk_android.data.local.TokenManager
import com.example.flowdesk_android.feature.auth.data.api.AuthApi
import com.example.flowdesk_android.feature.auth.data.dto.ChangePasswordRequest
import com.example.flowdesk_android.feature.auth.data.dto.LoginRequest
import com.example.flowdesk_android.feature.auth.data.dto.LogoutRequest
import com.example.flowdesk_android.feature.auth.data.dto.ProfileUpdateRequest
import com.example.flowdesk_android.feature.auth.data.dto.SignUpRequest
import com.example.flowdesk_android.feature.auth.domain.model.*
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager,
    private val sessionManager: com.example.flowdesk_android.data.local.SessionManager
) : AuthRepository {

    private val _sessionState = MutableStateFlow<AuthSession>(AuthSession.Guest)
    override val sessionState: StateFlow<AuthSession> = _sessionState.asStateFlow()


    override suspend fun login(command: LoginCommand): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(command.tenantName, command.userId, command.secret))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveToken(body.accessToken)
                    tokenManager.saveRefreshToken(body.refreshToken)
                    val authUser = body.user.toDomain(body.accessToken)
                    _sessionState.value = AuthSession.Active(authUser)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Login failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * TokenAuthenticator에서 refresh 실패 시 호출하여 세션을 강제로 Guest 전환합니다.
     * Data 레이어 내부 전용이며 Domain 레이어에는 노출되지 않습니다.
     */
    fun clearSessionDueToAuthFailure() {
        tokenManager.clear()
        _sessionState.value = AuthSession.Guest
        sessionManager.clear()
    }


    override suspend fun initializeSession() = withContext(Dispatchers.IO) {
        val token = tokenManager.getToken()
        if (token.isNullOrBlank()) {
            _sessionState.value = AuthSession.Guest
            return@withContext
        }
        try {
            val response = api.getMe()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val authUser = body.user.toDomain(token)
                    _sessionState.value = AuthSession.Active(authUser)
                    sessionManager.setSession(body.toDomain())
                } else {
                    _sessionState.value = AuthSession.Guest
                    sessionManager.clear()
                }
            } else {
                tokenManager.clear()
                _sessionState.value = AuthSession.Guest
            }
        } catch (e: Exception) {
            tokenManager.clear()
            _sessionState.value = AuthSession.Guest
        }
    }


    override suspend fun signUp(tenantName: String, companyName: String, adminName: String, email: String, phone: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.signUp(SignUpRequest(tenantName, companyName, adminName, email, phone, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.message)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("SignUp failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                 tokenManager.clear()
                 _sessionState.value = AuthSession.Guest
                 sessionManager.clear()
                 return@withContext Result.success(Unit)
            }
            val response = api.logout(LogoutRequest(refreshToken))
            if (response.isSuccessful) {
                tokenManager.clear()
                _sessionState.value = AuthSession.Guest
                sessionManager.clear()
                Result.success(Unit)
            } else {
                tokenManager.clear()
                _sessionState.value = AuthSession.Guest
                sessionManager.clear()
                Result.failure(Exception("Logout failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            tokenManager.clear()
            _sessionState.value = AuthSession.Guest
            sessionManager.clear()
            Result.failure(e)
        }
    }

    override suspend fun logoutAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
             val refreshToken = tokenManager.getRefreshToken() ?: ""
             val response = api.logoutAll(LogoutRequest(refreshToken))
             if (response.isSuccessful) {
                 tokenManager.clear()
                 _sessionState.value = AuthSession.Guest
                 sessionManager.clear()
                 Result.success(Unit)
             } else {
                 tokenManager.clear()
                 _sessionState.value = AuthSession.Guest
                 sessionManager.clear()
                 Result.failure(Exception("Logout all failed with code: ${response.code()}"))
             }
        } catch (e: Exception) {
            tokenManager.clear()
            _sessionState.value = AuthSession.Guest
            sessionManager.clear()
            Result.failure(e)
        }
    }

    override suspend fun getMe(): Result<AuthMeInfo> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMe()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val meInfo = body.toDomain()
                    sessionManager.setSession(meInfo)
                    Result.success(meInfo)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch user data with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(userName: String, userEmail: String, userTel: String?, userHp: String?): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val request = ProfileUpdateRequest(
                corpName = null,
                userName = userName,
                userEmail = userEmail,
                userTel = userTel,
                userHp = userHp
            )
            val response = api.updateProfile(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.toDomain())
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Update failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(current: String, new: String, confirm: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.changePassword(ChangePasswordRequest(current, new, confirm))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = if (response.code() == 400 || response.code() == 401) {
                     "현재 비밀번호가 틀렸습니다."
                } else {
                     "비밀번호 변경에 실패했습니다 (Code: ${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
