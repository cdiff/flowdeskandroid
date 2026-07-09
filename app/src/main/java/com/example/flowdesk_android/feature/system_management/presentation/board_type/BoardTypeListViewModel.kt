package com.example.flowdesk_android.feature.system_management.presentation.board_type

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class BoardTypeListViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    private val sessionManager: com.example.flowdesk_android.data.local.SessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _repoState = _refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                emit(null)
                emit(boardRepository.getBoardTypes())
            }
        }

    val uiState: StateFlow<BoardTypeListUiState> = combine(
        _repoState,
        sessionManager.observePermission("board_types.create"),
        sessionManager.observePermission("board_types.update"),
        sessionManager.observePermission("board_types.delete")
    ) { repoResult, canWrite, canUpdate, canDelete ->
        if (repoResult == null) {
            BoardTypeListUiState.Loading
        } else {
            repoResult.fold(
                onSuccess = { list ->
                    val filteredList = list.filter {
                        it.name.contains(_searchQuery.value, ignoreCase = true) ||
                                (it.description?.contains(_searchQuery.value, ignoreCase = true) ?: false)
                    }
                    val total = filteredList.size
                    val active = filteredList.count { it.isActive }
                    val inactive = total - active
                    BoardTypeListUiState.Success(filteredList, total, active, inactive, canWrite, canUpdate, canDelete)
                },
                onFailure = { err ->
                    BoardTypeListUiState.Error(err.message ?: "게시판 타입을 불러오지 못했습니다.")
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BoardTypeListUiState.Loading
    )

    init {
        // 검색어가 변경될 때마다 자동 갱신 트리거 (300ms 디바운스 적용)
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest {
                    triggerRefresh()
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }

    fun toggleBoardTypeActive(boardId: Long, currentActive: Boolean) {
        viewModelScope.launch {
            boardRepository.getBoardTypeDetail(boardId)
                .onSuccess { current ->
                    boardRepository.updateBoardType(
                        boardId = boardId,
                        name = current.name,
                        description = current.description,
                        sortOrder = current.sortOrder,
                        isActive = !currentActive
                    ).onSuccess {
                        _toastMessage.emit(if (!currentActive) "게시판이 활성화되었습니다." else "게시판이 비활성화되었습니다.")
                        triggerRefresh()
                    }.onFailure { err ->
                        _toastMessage.emit(err.message ?: "상태 변경에 실패했습니다.")
                    }
                }
                .onFailure { err ->
                    _toastMessage.emit(err.message ?: "정보 조회에 실패했습니다.")
                }
        }
    }

    fun deleteBoardType(boardId: Long) {
        viewModelScope.launch {
            boardRepository.deactivateBoardType(boardId)
                .onSuccess {
                    _toastMessage.emit("게시판이 삭제(비활성화)되었습니다.")
                    triggerRefresh()
                }
                .onFailure { err ->
                    _toastMessage.emit(err.message ?: "삭제에 실패했습니다.")
                }
        }
    }
}
