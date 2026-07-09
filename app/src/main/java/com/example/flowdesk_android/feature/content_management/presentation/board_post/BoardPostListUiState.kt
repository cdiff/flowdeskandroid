package com.example.flowdesk_android.feature.content_management.presentation.board_post

import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost

sealed class BoardPostListUiState {
    object Loading : BoardPostListUiState()
    
    data class Success(
        val boardTypes: List<BoardType>,
        val posts: List<BoardPost>,
        val totalCount: Int,
        val canWrite: Boolean = false   // boards.posts.create 권한
    ) : BoardPostListUiState()
    
    data class Error(val message: String) : BoardPostListUiState()
}
