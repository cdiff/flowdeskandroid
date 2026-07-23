package com.example.flowdesk_android.feature.system_management.domain.model

data class BoardPost(
    val postId: Long,
    val boardId: Long,
    val userSeq: Long,
    val title: String,
    val content: String?,
    val isNotice: Boolean,
    val isActive: Boolean,
    val startDtm: String?,
    val endDtm: String?,
    val createdAt: String?,
    val updatedAt: String?,
    // 추가: 게시글 카드 뱃지 표시 등을 위한 게시판 이름 필드 매핑용
    val boardName: String? = null
)
