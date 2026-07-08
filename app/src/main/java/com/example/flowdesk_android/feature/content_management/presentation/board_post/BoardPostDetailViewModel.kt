package com.example.flowdesk_android.feature.content_management.presentation.board_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BoardPostDetailUiState {
    object Idle : BoardPostDetailUiState()
    object Loading : BoardPostDetailUiState()
    data class Success(val post: BoardPost) : BoardPostDetailUiState()
    object SaveSuccess : BoardPostDetailUiState()
    object DeleteSuccess : BoardPostDetailUiState()
    data class Error(val message: String) : BoardPostDetailUiState()
}

@HiltViewModel
class BoardPostDetailViewModel @Inject constructor(
    private val boardRepository: BoardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BoardPostDetailUiState>(BoardPostDetailUiState.Idle)
    val uiState: StateFlow<BoardPostDetailUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun loadPostDetail(boardId: Long, postId: Long) {
        if (postId == -1L) {
            _uiState.value = BoardPostDetailUiState.Idle
            return
        }
        viewModelScope.launch {
            _uiState.value = BoardPostDetailUiState.Loading
            boardRepository.getBoardPostDetail(boardId, postId)
                .onSuccess {
                    _uiState.value = BoardPostDetailUiState.Success(it)
                }
                .onFailure { err ->
                    _uiState.value = BoardPostDetailUiState.Error(err.message ?: "게시글 정보를 가져오지 못했습니다.")
                }
        }
    }

    fun savePost(
        boardId: Long,
        postId: Long,
        title: String,
        content: String,
        isNotice: Boolean,
        isActive: Boolean,
        startDtm: String?,
        endDtm: String?
    ) {
        viewModelScope.launch {
            _uiState.value = BoardPostDetailUiState.Loading
            val result = if (postId == -1L) {
                boardRepository.createBoardPost(
                    boardId = boardId,
                    title = title,
                    content = content,
                    isNotice = isNotice,
                    startDtm = startDtm,
                    endDtm = endDtm
                )
            } else {
                boardRepository.updateBoardPost(
                    boardId = boardId,
                    postId = postId,
                    title = title,
                    content = content,
                    isNotice = isNotice,
                    isActive = isActive,
                    startDtm = startDtm,
                    endDtm = endDtm
                )
            }

            result.onSuccess {
                _uiState.value = BoardPostDetailUiState.SaveSuccess
            }.onFailure { err ->
                _uiState.value = BoardPostDetailUiState.Error(err.message ?: "저장에 실패했습니다.")
                _toastMessage.emit(err.message ?: "저장에 실패했습니다.")
            }
        }
    }

    fun deletePost(boardId: Long, postId: Long) {
        viewModelScope.launch {
            _uiState.value = BoardPostDetailUiState.Loading
            boardRepository.deleteBoardPost(boardId, postId)
                .onSuccess {
                    _uiState.value = BoardPostDetailUiState.DeleteSuccess
                }.onFailure { err ->
                    _uiState.value = BoardPostDetailUiState.Error(err.message ?: "삭제에 실패했습니다.")
                    _toastMessage.emit(err.message ?: "삭제에 실패했습니다.")
                }
        }
    }
}
