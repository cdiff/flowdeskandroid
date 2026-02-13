package com.example.flowdesk_android.data.repository

import com.example.flowdesk_android.data.local.TokenManager
import com.example.flowdesk_android.data.remote.AuthApi
import com.example.flowdesk_android.data.remote.dto.LoginRequest
import com.example.flowdesk_android.data.remote.dto.SignUpRequest
import com.example.flowdesk_android.data.remote.dto.AuthMeResponse
import com.example.flowdesk_android.domain.model.User
import com.example.flowdesk_android.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(tenantName: String, userId: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(tenantName, userId, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveToken(body.accessToken)
                    Result.success(User(
                        id = body.user.userSeq.toString(),
                        userId = body.user.userId,
                        email = body.user.userEmail,
                        name = body.user.userName,
                        corpName = body.user.corpName,
                        token = body.accessToken
                    ))
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

    override suspend fun signUp(companyName: String, adminName: String, email: String, phone: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.signUp(SignUpRequest(companyName, adminName, email, phone, password))
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

    override suspend fun getMe(): Result<AuthMeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMe()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
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
}
