package com.example.flowdesk_android.feature.system_management.presentation.board_type

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.local.SessionManager
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BoardTypeDetailUiState {
    object Idle : BoardTypeDetailUiState()
    object Loading : BoardTypeDetailUiState()
    data class Success(
        val boardType: BoardType,
        val canWrite: Boolean,   // 생성 권한 (board_types.create)
        val canUpdate: Boolean,  // 수정 권한 (board_types.update)
        val canDelete: Boolean   // 삭제 권한 (board_types.delete)
    ) : BoardTypeDetailUiState()
    object SaveSuccess : BoardTypeDetailUiState()
    data class Error(val message: String) : BoardTypeDetailUiState()
}

@HiltViewModel
class BoardTypeDetailViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _actionState = MutableStateFlow<BoardTypeDetailUiState>(BoardTypeDetailUiState.Idle)
    private val _loadedBoardType = MutableStateFlow<BoardType?>(null)

    val uiState: StateFlow<BoardTypeDetailUiState> = combine(
        _actionState,
        _loadedBoardType,
        sessionManager.observePermission("board_types.create"),
        sessionManager.observePermission("board_types.update"),
        sessionManager.observePermission("board_types.delete")
    ) { action, boardType, canWrite, canUpdate, canDelete ->
        when (action) {
            is BoardTypeDetailUiState.SaveSuccess -> BoardTypeDetailUiState.SaveSuccess
            is BoardTypeDetailUiState.Loading -> BoardTypeDetailUiState.Loading
            is BoardTypeDetailUiState.Error -> BoardTypeDetailUiState.Error(action.message)
            else -> {
                if (boardType != null) {
                    BoardTypeDetailUiState.Success(boardType, canWrite, canUpdate, canDelete)
                } else {
                    BoardTypeDetailUiState.Idle
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BoardTypeDetailUiState.Idle
    )

    /**
     * 생성 모드(boardId == -1L)에서는 ViewModel에 BoardType 데이터가 없기 때문에
     * uiState가 항상 Idle로 남는다. 생성 권한만 필요하므로 별도 StateFlow로 노출.
     */
    val canWrite: StateFlow<Boolean> = sessionManager.observePermission("board_types.create")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun loadBoardTypeDetail(boardId: Long) {
        if (boardId == -1L) {
            _actionState.value = BoardTypeDetailUiState.Idle
            _loadedBoardType.value = null
            return
        }
        viewModelScope.launch {
            _actionState.value = BoardTypeDetailUiState.Loading
            _loadedBoardType.value = null
            boardRepository.getBoardTypeDetail(boardId)
                .onSuccess {
                    _loadedBoardType.value = it
                    _actionState.value = BoardTypeDetailUiState.Idle
                }
                .onFailure { err ->
                    _actionState.value = BoardTypeDetailUiState.Error(err.message ?: "상세 정보를 조회하는 데 실패했습니다.")
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
            _actionState.value = BoardTypeDetailUiState.Loading
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
                _actionState.value = BoardTypeDetailUiState.SaveSuccess
            }.onFailure { err ->
                _actionState.value = BoardTypeDetailUiState.Error(err.message ?: "저장에 실패했습니다.")
                _toastMessage.emit(err.message ?: "저장에 실패했습니다.")
            }
        }
    }
}
