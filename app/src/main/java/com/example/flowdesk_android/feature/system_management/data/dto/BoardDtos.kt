package com.example.flowdesk_android.feature.system_management.data.dto

import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import com.example.flowdesk_android.feature.system_management.domain.model.WebsiteListResponse // 임시로 PageInfo 매핑용 혹은 core의 PageInfo 사용
import com.example.flowdesk_android.feature.system_management.domain.model.PageInfo
import com.google.gson.annotations.SerializedName

/**
 * 게시판 타입 API 응답 DTO
 */
data class BoardTypeDto(
    @SerializedName("boardId") val boardId: Long,
    @SerializedName("boardKey") val boardKey: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("isActive") val isActive: Int, // 1: 활성, 0: 비활성
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toDomain(): BoardType = BoardType(
        boardId = boardId,
        boardKey = boardKey,
        name = name,
        description = description,
        isActive = isActive == 1,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * 게시판 타입 목록 응답 DTO
 */
data class BoardTypeListResponseDto(
    @SerializedName("items") val items: List<BoardTypeDto>
)

/**
 * 게시판 타입 생성 요청 바디 DTO
 */
data class CreateBoardTypeRequestDto(
    @SerializedName("boardKey") val boardKey: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sortOrder") val sortOrder: Int
)

/**
 * 게시판 타입 수정 요청 바디 DTO
 */
data class UpdateBoardTypeRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("isActive") val isActive: Int
)

/**
 * 게시글 API 응답 DTO
 */
data class BoardPostDto(
    @SerializedName("postId") val postId: Long,
    @SerializedName("boardId") val boardId: Long,
    @SerializedName("userSeq") val userSeq: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String?,
    @SerializedName("isNotice") val isNotice: Int, // 1: 공지, 0: 일반
    @SerializedName("isActive") val isActive: Int, // 1: 활성, 0: 삭제/비활성
    @SerializedName("startDtm") val startDtm: String?,
    @SerializedName("endDtm") val endDtm: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toDomain(boardName: String? = null): BoardPost = BoardPost(
        postId = postId,
        boardId = boardId,
        userSeq = userSeq,
        title = title,
        content = content,
        isNotice = isNotice == 1,
        isActive = isActive == 1,
        startDtm = startDtm,
        endDtm = endDtm,
        createdAt = createdAt,
        updatedAt = updatedAt,
        boardName = boardName
    )
}

/**
 * 게시글 목록 응답 DTO
 */
data class BoardPostListResponseDto(
    @SerializedName("items") val items: List<BoardPostDto>,
    @SerializedName("pageInfo") val pageInfo: PageInfoDto
)

/**
 * 게시글 생성 요청 바디 DTO
 */
data class CreateBoardPostRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("isNotice") val isNotice: Int,
    @SerializedName("startDtm") val startDtm: String?,
    @SerializedName("endDtm") val endDtm: String?
)

/**
 * 게시글 수정 요청 바디 DTO
 */
data class UpdateBoardPostRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("isNotice") val isNotice: Int,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("startDtm") val startDtm: String?,
    @SerializedName("endDtm") val endDtm: String?
)
