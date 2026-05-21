package com.example.flowdesk_android.core.domain.repository

import com.example.flowdesk_android.core.domain.model.User
import com.example.flowdesk_android.core.domain.model.UserDetail

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserDetail(id: Int): Result<UserDetail>
    suspend fun createUser(
        userId: String,
        password: String,
        corpName: String,
        userName: String,
        userEmail: String,
        userTel: String,
        userHp: String,
        roleIds: List<Int>?
    ): Result<User>
    suspend fun updateUser(id: Int, corpName: String?, userName: String?, userEmail: String?, userTel: String?, userHp: String?): Result<Unit>
    suspend fun updateUserStatus(id: Int, isActive: Boolean): Result<Unit>
    suspend fun updateUserRoles(id: Int, add: List<Int>?, remove: List<Int>?): Result<Unit>
    suspend fun adminChangePassword(id: Int, newPassword: String): Result<Unit>
    suspend fun invalidateUserTokens(id: Int): Result<Unit>
}
