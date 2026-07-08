package com.example.flowdesk_android.feature.system_management.presentation.board_type

import com.example.flowdesk_android.feature.system_management.domain.model.BoardType

sealed class BoardTypeListUiState {
    object Loading : BoardTypeListUiState()
    data class Success(
        val items: List<BoardType>,
        val totalCount: Int,
        val activeCount: Int,
        val inactiveCount: Int
    ) : BoardTypeListUiState()
    data class Error(val message: String) : BoardTypeListUiState()
}
