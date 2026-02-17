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
    val userSeq: Int,
    val userId: String,
    val corpName: String,
    val userName: String,
    val userEmail: String,
    val userTel: String?,
    val userHp: String?,
    val isActive: Int,
    val tokenVersion: Int,
    val regDtm: String,
    val stopDtm: String?,
    val tenantId: Int
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
    val roles: List<String>,
    val permissions: Map<String, Boolean>,
    val menuTree: List<MenuDto>
)

data class MenuDto(
    val pageName: String,
    val displayName: String,
    val path: String,
    val order: Int,
    val children: List<MenuDto>
)

data class ProfileUpdateRequest(
    val corpName: String?, // Often read-only but included as per request
    val userName: String,
    val userEmail: String,
    val userTel: String?,
    val userHp: String?
)
