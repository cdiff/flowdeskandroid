package com.example.flowdesk_android.feature.super_admin.domain.model

data class Page(
    val pageId: Int,
    val parentId: Int?,
    val pageName: String,
    val path: String,
    val displayName: String,
    val description: String?,
    val isActive: Boolean,
    val sortOrder: Int,
    val childCount: Int,
    val permissionCount: Int,
    val createdAt: String?,
    val updatedAt: String?
)
