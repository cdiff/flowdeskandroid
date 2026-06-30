package com.example.flowdesk_android.feature.super_admin.data.dto

import com.example.flowdesk_android.feature.super_admin.domain.model.Page
import com.google.gson.annotations.SerializedName

// ──────────────────────────────────────────────────
// Request DTOs
// ──────────────────────────────────────────────────

data class CreatePageRequest(
    @SerializedName("pageName")    val pageName: String,
    @SerializedName("path")        val path: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("parentId")    val parentId: Int? = null,
    @SerializedName("sortOrder")   val sortOrder: Int = 1,
    @SerializedName("isActive")    val isActive: Int = 1
)

data class UpdatePageRequest(
    @SerializedName("pageName")    val pageName: String? = null,
    @SerializedName("path")        val path: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("parentId")    val parentId: Int? = null,
    @SerializedName("sortOrder")   val sortOrder: Int? = null,
    @SerializedName("isActive")    val isActive: Int? = null
)

// ──────────────────────────────────────────────────
// Response DTOs
// ──────────────────────────────────────────────────

data class PageDto(
    @SerializedName("pageId")           val pageId: Int = 0,
    @SerializedName("parentId")         val parentId: Int? = null,
    @SerializedName("pageName")         val pageName: String = "",
    @SerializedName("path")             val path: String = "",
    @SerializedName("displayName")      val displayName: String = "",
    @SerializedName("description")      val description: String? = null,
    @SerializedName("isActive")         val isActive: Int = 1,
    @SerializedName("sortOrder")        val sortOrder: Int = 0,
    @SerializedName("childCount")       val childCount: Int = 0,
    @SerializedName("permissionCount")  val permissionCount: Int = 0,
    @SerializedName("createdAt")        val createdAt: String? = null,
    @SerializedName("updatedAt")        val updatedAt: String? = null
) {
    fun toDomain() = Page(
        pageId          = pageId,
        parentId        = parentId,
        pageName        = pageName,
        path            = path,
        displayName     = displayName,
        description     = description,
        isActive        = isActive == 1,
        sortOrder       = sortOrder,
        childCount      = childCount,
        permissionCount = permissionCount,
        createdAt       = createdAt,
        updatedAt       = updatedAt
    )
}

data class PagesResponse(
    @SerializedName("items")    val items: List<PageDto>?,
    @SerializedName("pageInfo") val pageInfo: PageInfoDto?
)

data class PageInfoDto(
    @SerializedName("page")       val page: Int,
    @SerializedName("limit")      val limit: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPages") val totalPages: Int
)
