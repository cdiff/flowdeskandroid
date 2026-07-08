package com.example.flowdesk_android.feature.system_management.domain.repository

import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import com.example.flowdesk_android.feature.system_management.domain.model.PageInfo

interface BoardRepository {

    // ─── 게시판 타입 관련 인터페이스 ───
    suspend fun getBoardTypes(): Result<List<BoardType>>
    
    suspend fun createBoardType(
        boardKey: String,
        name: String,
        description: String?,
        sortOrder: Int
    ): Result<BoardType>
    
    suspend fun getBoardTypeDetail(boardId: Long): Result<BoardType>
    
    suspend fun updateBoardType(
        boardId: Long,
        name: String,
        description: String?,
        sortOrder: Int,
        isActive: Boolean
    ): Result<BoardType>
    
    suspend fun deactivateBoardType(boardId: Long): Result<Unit>

    // ─── 게시글 관련 인터페이스 ───
    suspend fun getBoardPosts(
        boardId: Long,
        page: Int,
        limit: Int,
        query: String?
    ): Result<Pair<List<BoardPost>, PageInfo>>
    
    suspend fun createBoardPost(
        boardId: Long,
        title: String,
        content: String,
        isNotice: Boolean,
        startDtm: String?,
        endDtm: String?
    ): Result<BoardPost>
    
    suspend fun getBoardPostDetail(boardId: Long, postId: Long): Result<BoardPost>
    
    suspend fun updateBoardPost(
        boardId: Long,
        postId: Long,
        title: String,
        content: String,
        isNotice: Boolean,
        isActive: Boolean,
        startDtm: String?,
        endDtm: String?
    ): Result<BoardPost>
    
    suspend fun deleteBoardPost(boardId: Long, postId: Long): Result<Unit>
}
