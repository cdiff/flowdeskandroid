package com.example.flowdesk_android.feature.content_management.presentation.board_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.local.SessionManager
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BoardPostDetailUiState {
    object Idle : BoardPostDetailUiState()
    object Loading : BoardPostDetailUiState()
    data class Success(
        val post: BoardPost,
        val canUpdate: Boolean,  // boards.posts.update 권한
        val canDelete: Boolean   // boards.posts.delete 권한
    ) : BoardPostDetailUiState()
    object SaveSuccess : BoardPostDetailUiState()
    object DeleteSuccess : BoardPostDetailUiState()
    data class Error(val message: String) : BoardPostDetailUiState()
}

@HiltViewModel
class BoardPostDetailViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _actionState = MutableStateFlow<BoardPostDetailUiState>(BoardPostDetailUiState.Idle)
    private val _loadedPost = MutableStateFlow<BoardPost?>(null)

    /** 생성 모드(postId == -1L)에서 사용할 create 권한 */
    val canWrite: StateFlow<Boolean> = sessionManager.observePermission("boards.posts.create")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /** 상세/수정 모드에서 update + delete 권한을 combine으로 묶어 uiState에 포함 */
    val uiState: StateFlow<BoardPostDetailUiState> = combine(
        _actionState,
        _loadedPost,
        sessionManager.observePermission("boards.posts.update"),
        sessionManager.observePermission("boards.posts.delete")
    ) { action, post, canUpdate, canDelete ->
        when (action) {
            is BoardPostDetailUiState.SaveSuccess -> BoardPostDetailUiState.SaveSuccess
            is BoardPostDetailUiState.DeleteSuccess -> BoardPostDetailUiState.DeleteSuccess
            is BoardPostDetailUiState.Loading -> BoardPostDetailUiState.Loading
            is BoardPostDetailUiState.Error -> BoardPostDetailUiState.Error(action.message)
            else -> {
                if (post != null) {
                    BoardPostDetailUiState.Success(post, canUpdate, canDelete)
                } else {
                    BoardPostDetailUiState.Idle
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BoardPostDetailUiState.Idle
    )

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun loadPostDetail(boardId: Long, postId: Long) {
        if (postId == -1L) {
            _actionState.value = BoardPostDetailUiState.Idle
            _loadedPost.value = null
            return
        }
        viewModelScope.launch {
            _actionState.value = BoardPostDetailUiState.Loading
            _loadedPost.value = null
            boardRepository.getBoardPostDetail(boardId, postId)
                .onSuccess {
                    _loadedPost.value = it
                    _actionState.value = BoardPostDetailUiState.Idle
                }
                .onFailure { err ->
                    _actionState.value = BoardPostDetailUiState.Error(err.message ?: "게시글 정보를 가져오지 못했습니다.")
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
            _actionState.value = BoardPostDetailUiState.Loading
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
                _actionState.value = BoardPostDetailUiState.SaveSuccess
            }.onFailure { err ->
                _actionState.value = BoardPostDetailUiState.Error(err.message ?: "저장에 실패했습니다.")
                _toastMessage.emit(err.message ?: "저장에 실패했습니다.")
            }
        }
    }

    fun deletePost(boardId: Long, postId: Long) {
        viewModelScope.launch {
            _actionState.value = BoardPostDetailUiState.Loading
            boardRepository.deleteBoardPost(boardId, postId)
                .onSuccess {
                    _actionState.value = BoardPostDetailUiState.DeleteSuccess
                }.onFailure { err ->
                    _actionState.value = BoardPostDetailUiState.Error(err.message ?: "삭제에 실패했습니다.")
                    _toastMessage.emit(err.message ?: "삭제에 실패했습니다.")
                }
        }
    }
}
