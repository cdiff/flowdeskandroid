package com.example.flowdesk_android.feature.user_management.domain.model

data class User(
    val userSeq: Int,
    val userId: String,
    val userName: String,
    val userEmail: String?,
    val corpName: String?,
    val userTel: String?,
    val userHp: String?,
    val isActive: Boolean,
    val roles: List<UserRole>,
    val regDtm: String?
)

data class UserDetail(
    val userSeq: Int,
    val userId: String,
    val userName: String,
    val userEmail: String?,
    val corpName: String?,
    val userTel: String?,
    val userHp: String?,
    val isActive: Boolean,
    val tokenVersion: Int,
    val regDtm: String?,
    val stopDtm: String?,
    val assignedRoleIds: List<Int>,
    val availableRoles: List<UserRole>
)

data class UserRole(
    val roleId: Int,
    val roleName: String,
    val displayName: String,
    val isActive: Boolean,
    val isAssigned: Boolean = false
)
