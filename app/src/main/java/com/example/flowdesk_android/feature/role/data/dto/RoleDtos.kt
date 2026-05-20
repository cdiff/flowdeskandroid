package com.example.flowdesk_android.feature.role.data.dto

import com.example.flowdesk_android.feature.role.domain.model.CatalogAction
import com.example.flowdesk_android.feature.role.domain.model.CatalogPage
import com.example.flowdesk_android.feature.role.domain.model.CatalogPermission
import com.example.flowdesk_android.feature.role.domain.model.PermissionAction
import com.example.flowdesk_android.feature.role.domain.model.PermissionCatalog
import com.example.flowdesk_android.feature.role.domain.model.PermissionPage
import com.example.flowdesk_android.feature.role.domain.model.Role
import com.example.flowdesk_android.feature.role.domain.model.RoleAssignedUser
import com.example.flowdesk_android.feature.role.domain.model.RoleDetail
import com.google.gson.annotations.SerializedName

// ── Requests ────────────────────────────────────────────

data class CreateRoleRequest(
    @SerializedName("roleName") val roleName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String
)

data class UpdateRoleStatusRequest(
    @SerializedName("isActive") val isActive: Int
)

data class UpdateRoleInfoRequest(
    @SerializedName("roleName") val roleName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String?
)

data class UpdateRolePermissionsRequest(
    @SerializedName("add") val add: List<Int>? = null,
    @SerializedName("remove") val remove: List<Int>? = null
)

data class CopyRolePermissionsRequest(
    @SerializedName("sourceRoleId") val sourceRoleId: Int
)

// ── Responses ───────────────────────────────────────────

data class RoleDto(
    @SerializedName("roleId") val roleId: Int,
    @SerializedName("roleName") val roleName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("userCount") val userCount: Int?,
    @SerializedName("permissionCount") val permissionCount: Int?,
    @SerializedName("createdAt") val createdAt: String?
) {
    fun toDomain() = Role(
        roleId = roleId,
        roleName = roleName,
        displayName = displayName,
        description = description,
        isActive = isActive == 1,
        userCount = userCount ?: 0,
        permissionCount = permissionCount ?: 0,
        createdAt = createdAt
    )
}

data class RolesResponse(
    @SerializedName("items") val items: List<RoleDto>?,
    @SerializedName("pageInfo") val pageInfo: PageInfoDto?
)

data class PageInfoDto(
    @SerializedName("page") val page: Int,
    @SerializedName("totalItems") val totalItems: Int
)

data class RoleDetailResponse(
    @SerializedName("roleId") val roleId: Int,
    @SerializedName("roleName") val roleName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("permissionsByPage") val permissionsByPage: List<PermissionPageDto>?,
    @SerializedName("assignedUsers") val assignedUsers: List<RoleAssignedUserDto>?
) {
    fun toDomain() = RoleDetail(
        roleId = roleId,
        roleName = roleName,
        displayName = displayName,
        description = description,
        isActive = isActive == 1,
        permissionsByPage = permissionsByPage?.map { it.toDomain() } ?: emptyList(),
        assignedUsers = assignedUsers?.map { it.toDomain() } ?: emptyList()
    )
}

data class PermissionPageDto(
    @SerializedName("pageId") val pageId: Int,
    @SerializedName("pageName") val pageName: String,
    @SerializedName("pageDisplayName") val pageDisplayName: String,
    @SerializedName("permissions") val permissions: List<PermissionActionDto>?
) {
    fun toDomain() = PermissionPage(
        pageId = pageId,
        pageName = pageName,
        pageDisplayName = pageDisplayName,
        permissions = permissions?.map { it.toDomain() } ?: emptyList()
    )
}

data class PermissionActionDto(
    @SerializedName("permissionId") val permissionId: Int,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("actionName") val actionName: String,
    @SerializedName("actionDisplayName") val actionDisplayName: String
) {
    fun toDomain() = PermissionAction(
        permissionId = permissionId,
        displayName = displayName,
        description = description,
        actionName = actionName,
        actionDisplayName = actionDisplayName
    )
}

data class RoleAssignedUserDto(
    @SerializedName("userSeq") val userSeq: Int,
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("isActive") val isActive: Int
) {
    fun toDomain() = RoleAssignedUser(
        userSeq = userSeq,
        userId = userId,
        userName = userName,
        isActive = isActive == 1
    )
}

data class PermissionCatalogResponse(
    @SerializedName("pages") val pages: List<PageDto>,
    @SerializedName("actions") val actions: List<ActionDto>,
    @SerializedName("permissions") val permissions: List<PermissionDto>
) {
    fun toDomain() = PermissionCatalog(
        pages = pages.map { it.toDomain() },
        actions = actions.map { it.toDomain() },
        permissions = permissions.map { it.toDomain() }
    )
}

data class PageDto(
    @SerializedName("pageId") val pageId: Int,
    @SerializedName("pageName") val pageName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String?
) {
    fun toDomain() = CatalogPage(pageId, pageName, displayName, description)
}

data class ActionDto(
    @SerializedName("actionId") val actionId: Int,
    @SerializedName("actionName") val actionName: String,
    @SerializedName("displayName") val displayName: String
) {
    fun toDomain() = CatalogAction(actionId, actionName, displayName)
}

data class PermissionDto(
    @SerializedName("permissionId") val permissionId: Int,
    @SerializedName("pageId") val pageId: Int,
    @SerializedName("actionId") val actionId: Int,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String?
) {
    fun toDomain() = CatalogPermission(permissionId, pageId, actionId, displayName, description)
}
