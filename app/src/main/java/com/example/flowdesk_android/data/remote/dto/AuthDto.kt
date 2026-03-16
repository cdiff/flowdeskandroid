package com.example.flowdesk_android.data.remote.dto

data class LoginRequest(
    val tenantName: String,
    val userId: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val expiresIn: String,
    val refreshToken: String,
    val refreshExpiresAt: String,
    val user: UserDto
)

data class UserDto(
    val userSeq: Int = 0,
    val userId: String = "",
    val corpName: String? = null,
    val userName: String = "",
    val userEmail: String? = null,
    val userTel: String? = null,
    val userHp: String? = null,
    val isActive: Int = 0,
    val tokenVersion: Int = 0,
    val regDtm: String? = null,
    val stopDtm: String? = null,
    val tenantId: Int = 0
)

data class SignUpRequest(
    val companyName: String,
    val adminName: String,
    val email: String,
    val phone: String,
    val password: String
)

data class SignUpResponse(
    val message: String,
    val tenant: TenantDto,
    val admin: AdminDto
)

data class TenantDto(
    val tenantId: Int,
    val tenantName: String
)

data class AdminDto(
    val userSeq: Int,
    val userId: String,
    val userName: String
)

data class AuthMeResponse(
    val user: UserDto,
    val roles: List<String>?,
    val permissions: Map<String, Boolean>?,
    val menuTree: List<MenuDto>?
)

data class MenuDto(
    val pageName: String,
    val displayName: String,
    val path: String,
    val order: Int,
    val children: List<MenuDto>
)

data class ProfileUpdateRequest(
    val corpName: String?,
    val userName: String,
    val userEmail: String,
    val userTel: String?,
    val userHp: String?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class UsersResponse(
    val items: List<UserDto>?,
    val pageInfo: PageInfoDto?
)

data class PageInfoDto(
    val page: Int,
    val limit: Int,
    val totalItems: Int,
    val totalPages: Int
)

data class CreateUserRequest(
    val userId: String,
    val password: String,
    val corpName: String,
    val userName: String,
    val userEmail: String,
    val userTel: String,
    val userHp: String
)
