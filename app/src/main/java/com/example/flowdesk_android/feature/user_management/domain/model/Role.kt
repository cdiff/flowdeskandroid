package com.example.flowdesk_android.feature.user_management.domain.model

data class Role(
    val roleId: Int,
    val roleName: String,
    val displayName: String,
    val description: String?,
    val isActive: Boolean,
    val userCount: Int,
    val permissionCount: Int,
    val createdAt: String?
)

data class RoleDetail(
    val roleId: Int,
    val roleName: String,
    val displayName: String,
    val description: String?,
    val isActive: Boolean,
    val permissionsByPage: List<PermissionPage>,
    val assignedUsers: List<RoleAssignedUser>
)

data class PermissionPage(
    val pageId: Int,
    val pageName: String,
    val pageDisplayName: String,
    val permissions: List<PermissionAction>
)

data class PermissionAction(
    val permissionId: Int,
    val displayName: String,
    val description: String?,
    val actionName: String,
    val actionDisplayName: String
)

data class RoleAssignedUser(
    val userSeq: Int,
    val userId: String,
    val userName: String,
    val isActive: Boolean
)

data class PermissionCatalog(
    val pages: List<CatalogPage>,
    val actions: List<CatalogAction>,
    val permissions: List<CatalogPermission>
)

data class CatalogPage(
    val pageId: Int,
    val pageName: String,
    val displayName: String,
    val description: String?
)

data class CatalogAction(
    val actionId: Int,
    val actionName: String,
    val displayName: String
)

data class CatalogPermission(
    val permissionId: Int,
    val pageId: Int,
    val actionId: Int,
    val displayName: String,
    val description: String?
)
