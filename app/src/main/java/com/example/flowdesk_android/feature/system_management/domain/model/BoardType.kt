package com.example.flowdesk_android.feature.system_management.domain.model

data class BoardType(
    val boardId: Long,
    val boardKey: String,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: String?,
    val updatedAt: String?
)
