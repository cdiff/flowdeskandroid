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
    val tenantId: Int = 0,
    val roles: List<RoleDto>? = null
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

data class UpdateUserInfoRequest(
    val corpName: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val userTel: String? = null,
    val userHp: String? = null,
    val roleIds: List<Int>? = null
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
    val userHp: String,
    val roleIds: List<Int>? = null
)

data class RoleDto(
    val roleId: Int,
    val roleName: String,
    val displayName: String,
    val description: String,
    val isActive: Int,
    val isAssigned: Boolean = false,
    val createdAt: String?,
    val updatedAt: String?,
    val tenantId: Int?,
    val userCount: Int?,
    val permissionCount: Int?
)

data class CreateRoleRequest(
    val roleName: String,
    val displayName: String,
    val description: String
)

data class RolesResponse(
    val items: List<RoleDto>?,
    val pageInfo: PageInfoDto?
)

data class UserDetailDto(
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
    val tenantId: Int = 0,
    val assignedRoleIds: List<Int>? = null,
    val availableRoles: List<RoleDto>? = null
)

data class UpdateUserStatusRequest(
    val isActive: Int
)

data class UpdateUserRolesRequest(
    val add: List<Int>? = null,
    val remove: List<Int>? = null
)

data class AdminChangePasswordRequest(
    val newPassword: String
)

data class UpdateRoleStatusRequest(
    val isActive: Int
)

data class UpdateRoleInfoRequest(
    val roleName: String,
    val displayName: String,
    val description: String?
)

data class UpdateRolePermissionsRequest(
    val add: List<Int>? = null,
    val remove: List<Int>? = null
)

data class RoleDetailResponse(
    val roleId: Int,
    val roleName: String,
    val displayName: String,
    val description: String?,
    val isActive: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val tenantId: Int?,
    val permissionsByPage: List<PermissionPageDto>?,
    val assignedUsers: List<RoleAssignedUserDto>?
)

data class PermissionPageDto(
    val pageId: Int,
    val pageName: String,
    val pageDisplayName: String,
    val permissions: List<PermissionActionDto>?
)

data class PermissionActionDto(
    val permissionId: Int,
    val displayName: String,
    val description: String?,
    val actionId: Int,
    val actionName: String,
    val actionDisplayName: String
)

data class RoleAssignedUserDto(
    val userSeq: Int,
    val userId: String,
    val userName: String,
    val email: String?,
    val isActive: Int,
    val assignedAt: String?
)

data class PermissionCatalogResponse(
    val pages: List<PageDto>,
    val actions: List<ActionDto>,
    val permissions: List<PermissionDto>,
    val matrix: Map<String, List<MatrixActionDto>>
)

data class PageDto(
    val pageId: Int,
    val parentId: Int?,
    val pageName: String,
    val path: String?,
    val displayName: String,
    val description: String?,
    val sortOrder: Int
)

data class ActionDto(
    val actionId: Int,
    val actionName: String,
    val displayName: String
)

data class PermissionDto(
    val permissionId: Int,
    val pageId: Int,
    val actionId: Int,
    val displayName: String,
    val description: String?
)

data class MatrixActionDto(
    val actionName: String,
    val permissionId: Int
)

data class CopyRolePermissionsRequest(
    val sourceRoleId: Int
)
