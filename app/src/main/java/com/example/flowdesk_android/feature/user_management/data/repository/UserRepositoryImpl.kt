package com.example.flowdesk_android.feature.user_management.data.repository

import com.example.flowdesk_android.feature.user_management.data.api.UserApi
import com.example.flowdesk_android.feature.user_management.data.dto.AdminChangePasswordRequest
import com.example.flowdesk_android.feature.user_management.data.dto.CreateUserRequest
import com.example.flowdesk_android.feature.user_management.data.dto.UpdateUserInfoRequest
import com.example.flowdesk_android.feature.user_management.data.dto.UpdateUserRolesRequest
import com.example.flowdesk_android.feature.user_management.data.dto.UpdateUserStatusRequest
import com.example.flowdesk_android.feature.user_management.domain.model.User
import com.example.flowdesk_android.feature.user_management.domain.model.UserDetail
import com.example.flowdesk_android.feature.user_management.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getUsers()
            if (response.isSuccessful) {
                response.body()?.items?.map { it.toDomain() } ?: emptyList()
            } else {
                throw Exception("유저 목록 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun getUserDetail(id: Int): Result<UserDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getUserDetail(id)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("유저 상세 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun createUser(
        userId: String, password: String, corpName: String,
        userName: String, userEmail: String, userTel: String,
        userHp: String, roleIds: List<Int>?
    ): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val request = CreateUserRequest(userId, password, corpName, userName, userEmail, userTel, userHp, roleIds)
            val response = api.createUser(request)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("유저 생성 실패 (${response.code()})")
            }
        }
    }

    override suspend fun updateUser(
        id: Int, corpName: String?, userName: String?,
        userEmail: String?, userTel: String?, userHp: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateUser(id, UpdateUserInfoRequest(corpName, userName, userEmail, userTel, userHp))
            if (!response.isSuccessful) throw Exception("유저 정보 수정 실패 (${response.code()})")
        }
    }

    override suspend fun updateUserStatus(id: Int, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val statusValue = if (isActive) 1 else 0
            val response = api.updateUserStatus(id, UpdateUserStatusRequest(statusValue))
            if (!response.isSuccessful) throw Exception("상태 변경 실패 (${response.code()})")
        }
    }

    override suspend fun updateUserRoles(id: Int, add: List<Int>?, remove: List<Int>?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateUserRoles(id, UpdateUserRolesRequest(add, remove))
            if (!response.isSuccessful) throw Exception("역할 변경 실패 (${response.code()})")
        }
    }

    override suspend fun adminChangePassword(id: Int, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.adminChangePassword(id, AdminChangePasswordRequest(newPassword))
            if (!response.isSuccessful) throw Exception("비밀번호 변경 실패 (${response.code()})")
        }
    }

    override suspend fun invalidateUserTokens(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.invalidateUserTokens(id)
            if (!response.isSuccessful) throw Exception("토큰 무효화 실패 (${response.code()})")
        }
    }
}
