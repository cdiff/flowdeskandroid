package com.example.flowdesk_android.feature.super_admin.domain.model

data class Action(
    val actionId: Int,
    val actionName: String,
    val displayName: String,
    val isActive: Boolean,
    val permissionCount: Int,
    val createdAt: String?,
    val updatedAt: String?
)
