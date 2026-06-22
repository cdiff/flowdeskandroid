package com.example.flowdesk_android.feature.user_management.data.dto

import com.example.flowdesk_android.feature.user_management.domain.model.User
import com.example.flowdesk_android.feature.user_management.domain.model.UserDetail
import com.example.flowdesk_android.feature.user_management.domain.model.UserRole
import com.google.gson.annotations.SerializedName

// ──────────────────────────────────────────────────
// Request DTOs
// ──────────────────────────────────────────────────

data class CreateUserRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("password") val password: String,
    @SerializedName("corpName") val corpName: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userTel") val userTel: String,
    @SerializedName("userHp") val userHp: String,
    @SerializedName("roleIds") val roleIds: List<Int>? = null
)

data class UpdateUserInfoRequest(
    @SerializedName("corpName") val corpName: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("userTel") val userTel: String? = null,
    @SerializedName("userHp") val userHp: String? = null,
    @SerializedName("roleIds") val roleIds: List<Int>? = null
)

data class UpdateUserStatusRequest(
    @SerializedName("isActive") val isActive: Int
)

data class UpdateUserRolesRequest(
    @SerializedName("add") val add: List<Int>? = null,
    @SerializedName("remove") val remove: List<Int>? = null
)

data class AdminChangePasswordRequest(
    @SerializedName("newPassword") val newPassword: String
)

// ──────────────────────────────────────────────────
// Response DTOs
// ──────────────────────────────────────────────────

data class UserDto(
    @SerializedName("userSeq") val userSeq: Int = 0,
    @SerializedName("userId") val userId: String = "",
    @SerializedName("corpName") val corpName: String? = null,
    @SerializedName("userName") val userName: String = "",
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("userTel") val userTel: String? = null,
    @SerializedName("userHp") val userHp: String? = null,
    @SerializedName("isActive") val isActive: Int = 0,
    @SerializedName("regDtm") val regDtm: String? = null,
    @SerializedName("roles") val roles: List<RoleSimpleDto>? = null
) {
    fun toDomain() = User(
        userSeq = userSeq,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        corpName = corpName,
        userTel = userTel,
        userHp = userHp,
        isActive = isActive == 1,
        roles = roles?.map { it.toDomain() } ?: emptyList(),
        regDtm = regDtm
    )
}

data class UserDetailDto(
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
    @SerializedName("tenantId") val tenantId: Int = 0,
    @SerializedName("assignedRoleIds") val assignedRoleIds: List<Int>? = null,
    @SerializedName("availableRoles") val availableRoles: List<RoleSimpleDto>? = null
) {
    fun toDomain() = UserDetail(
        userSeq = userSeq,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        corpName = corpName,
        userTel = userTel,
        userHp = userHp,
        isActive = isActive == 1,
        tokenVersion = tokenVersion,
        regDtm = regDtm,
        stopDtm = stopDtm,
        assignedRoleIds = assignedRoleIds ?: emptyList(),
        availableRoles = availableRoles?.map { it.toDomain() } ?: emptyList()
    )
}

data class RoleSimpleDto(
    @SerializedName("roleId") val roleId: Int,
    @SerializedName("roleName") val roleName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("isActive") val isActive: Int = 1,
    @SerializedName("isAssigned") val isAssigned: Boolean = false
) {
    fun toDomain() = UserRole(
        roleId = roleId,
        roleName = roleName,
        displayName = displayName,
        isActive = isActive == 1,
        isAssigned = isAssigned
    )
}

data class UsersResponse(
    @SerializedName("items") val items: List<UserDto>?,
    @SerializedName("pageInfo") val pageInfo: PageInfoDto?
)


