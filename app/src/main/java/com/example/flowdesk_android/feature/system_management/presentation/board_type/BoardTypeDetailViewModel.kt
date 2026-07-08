package com.example.flowdesk_android.feature.system_management.presentation.board_type

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BoardTypeDetailUiState {
    object Idle : BoardTypeDetailUiState()
    object Loading : BoardTypeDetailUiState()
    data class Success(val boardType: BoardType) : BoardTypeDetailUiState()
    object SaveSuccess : BoardTypeDetailUiState()
    data class Error(val message: String) : BoardTypeDetailUiState()
}

@HiltViewModel
class BoardTypeDetailViewModel @Inject constructor(
    private val boardRepository: BoardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BoardTypeDetailUiState>(BoardTypeDetailUiState.Idle)
    val uiState: StateFlow<BoardTypeDetailUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun loadBoardTypeDetail(boardId: Long) {
        if (boardId == -1L) {
            _uiState.value = BoardTypeDetailUiState.Idle
            return
        }
        viewModelScope.launch {
            _uiState.value = BoardTypeDetailUiState.Loading
            boardRepository.getBoardTypeDetail(boardId)
                .onSuccess {
                    _uiState.value = BoardTypeDetailUiState.Success(it)
                }
                .onFailure { err ->
                    _uiState.value = BoardTypeDetailUiState.Error(err.message ?: "상세 정보를 조회하는 데 실패했습니다.")
                }
        }
    }

    fun saveBoardType(
        boardId: Long,
        boardKey: String,
        name: String,
        description: String?,
        sortOrder: Int,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = BoardTypeDetailUiState.Loading
            val result = if (boardId == -1L) {
                // 추가 모드
                boardRepository.createBoardType(
                    boardKey = boardKey,
                    name = name,
                    description = description,
                    sortOrder = sortOrder
                )
            } else {
                // 수정 모드
                boardRepository.updateBoardType(
                    boardId = boardId,
                    name = name,
                    description = description,
                    sortOrder = sortOrder,
                    isActive = isActive
                )
            }

            result.onSuccess {
                _uiState.value = BoardTypeDetailUiState.SaveSuccess
            }.onFailure { err ->
                _uiState.value = BoardTypeDetailUiState.Error(err.message ?: "저장에 실패했습니다.")
                _toastMessage.emit(err.message ?: "저장에 실패했습니다.")
            }
        }
    }
}
