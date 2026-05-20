package com.example.flowdesk_android.feature.auth.data.dto

import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.auth.domain.model.AuthUser
import com.example.flowdesk_android.feature.auth.domain.model.Menu
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("tenantName") val tenantName: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("expiresIn") val expiresIn: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("refreshExpiresAt") val refreshExpiresAt: String,
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("userSeq") val userSeq: Int = 0,
    @SerializedName("userId") val userId: String = "",
    @SerializedName("corpName") val corpName: String? = null,
    @SerializedName("userName") val userName: String = "",
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("userTel") val userTel: String? = null,
    @SerializedName("userHp") val userHp: String? = null,
    @SerializedName("isActive") val isActive: Int = 0,
    @SerializedName("tokenVersion") val tokenVersion: Int = 0,
    @SerializedName("regDtm") val regDtm: String? = null,
    @SerializedName("stopDtm") val stopDtm: String? = null,
    @SerializedName("tenantId") val tenantId: Int = 0
) {
    fun toDomain(token: String = ""): AuthUser {
        return AuthUser(
            id = userSeq.toString(),
            userId = userId,
            email = userEmail ?: "",
            name = userName,
            corpName = corpName ?: "",
            token = token,
            tel = userTel,
            hp = userHp
        )
    }
}

data class SignUpRequest(
    @SerializedName("tenantName") val tenantName: String,
    @SerializedName("companyName") val companyName: String,
    @SerializedName("adminName") val adminName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String
)

data class SignUpResponse(
    @SerializedName("message") val message: String,
    @SerializedName("tenant") val tenant: TenantDto,
    @SerializedName("admin") val admin: AdminDto
)

data class TenantDto(
    @SerializedName("tenantId") val tenantId: Int,
    @SerializedName("tenantName") val tenantName: String
)

data class AdminDto(
    @SerializedName("userSeq") val userSeq: Int,
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String
)

data class AuthMeResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("roles") val roles: List<String>?,
    @SerializedName("permissions") val permissions: Map<String, Boolean>?,
    @SerializedName("menuTree") val menuTree: List<MenuDto>?
) {
    fun toDomain(): AuthMeInfo {
        return AuthMeInfo(
            user = user.toDomain(),
            roles = roles ?: emptyList(),
            permissions = permissions ?: emptyMap(),
            menuTree = menuTree?.map { it.toDomain() } ?: emptyList()
        )
    }
}

data class MenuDto(
    @SerializedName("pageName") val pageName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("path") val path: String,
    @SerializedName("order") val order: Int,
    @SerializedName("children") val children: List<MenuDto>?
) {
    fun toDomain(): Menu {
        return Menu(
            pageName = pageName,
            displayName = displayName,
            path = path,
            order = order,
            children = children?.map { it.toDomain() } ?: emptyList()
        )
    }
}

data class ProfileUpdateRequest(
    @SerializedName("corpName") val corpName: String?,
    @SerializedName("userName") val userName: String,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userTel") val userTel: String?,
    @SerializedName("userHp") val userHp: String?
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)

data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String
)
