package com.example.flowdesk_android.data.repository

import com.example.flowdesk_android.data.local.TokenManager
import com.example.flowdesk_android.data.remote.AuthApi
import com.example.flowdesk_android.data.remote.dto.LoginRequest
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

    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveToken(body.accessToken)
                    Result.success(User(body.id, body.email, body.name, body.accessToken))
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
}
