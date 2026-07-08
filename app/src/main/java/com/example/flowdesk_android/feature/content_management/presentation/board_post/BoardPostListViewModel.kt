package com.example.flowdesk_android.feature.content_management.presentation.board_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class BoardPostListViewModel @Inject constructor(
    private val boardRepository: BoardRepository
) : ViewModel() {

    private val _boardTypes = MutableStateFlow<List<BoardType>>(emptyList())
    val boardTypes: StateFlow<List<BoardType>> = _boardTypes.asStateFlow()

    private val _selectedBoardId = MutableStateFlow<Long>(-1L)
    val selectedBoardId: StateFlow<Long> = _selectedBoardId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    val uiState: StateFlow<BoardPostListUiState> = combine(
        _selectedBoardId,
        _currentPage,
        _refreshTrigger.onStart { emit(Unit) }
    ) { boardId, page, _ -> Triple(boardId, page, _searchQuery.value) }
        .flatMapLatest { (boardId, page, query) ->
            flow {
                if (_boardTypes.value.isEmpty()) {
                    emit(BoardPostListUiState.Loading)
                    boardRepository.getBoardTypes()
                        .onSuccess { types ->
                            val activeTypes = types.filter { it.isActive }
                            _boardTypes.value = activeTypes
                            if (activeTypes.isNotEmpty() && _selectedBoardId.value == -1L) {
                                _selectedBoardId.value = activeTypes.first().boardId
                            }
                        }
                        .onFailure { err ->
                            emit(BoardPostListUiState.Error(err.message ?: "게시판 목록을 조회하지 못했습니다."))
                            return@flow
                        }
                }

                val currentBoardId = _selectedBoardId.value
                if (currentBoardId == -1L) {
                    emit(BoardPostListUiState.Success(_boardTypes.value, emptyList(), 0))
                    return@flow
                }

                emit(BoardPostListUiState.Loading)
                boardRepository.getBoardPosts(currentBoardId, page, 20, query.ifEmpty { null })
                    .onSuccess { (posts, pageInfo) ->
                        // 공지글 우선 정렬 (isNotice == true인 항목을 상단으로)
                        val sortedPosts = posts.sortedWith(compareByDescending<BoardPost> { it.isNotice }.thenByDescending { it.createdAt })
                        
                        // 각 포스트에 게시판 이름 매핑 추가
                        val currentBoardName = _boardTypes.value.find { it.boardId == currentBoardId }?.name
                        val mappedPosts = sortedPosts.map { it.copy(boardName = currentBoardName) }
                        
                        emit(BoardPostListUiState.Success(_boardTypes.value, mappedPosts, pageInfo.totalItems))
                    }
                    .onFailure { err ->
                        emit(BoardPostListUiState.Error(err.message ?: "게시글 목록을 불러오지 못했습니다."))
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BoardPostListUiState.Loading
        )

    init {
        // 검색어 디바운스 처리 후 목록 갱신
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest {
                    _currentPage.value = 1
                    triggerRefresh()
                }
        }
    }

    fun selectBoard(boardId: Long) {
        if (_selectedBoardId.value == boardId) return
        _selectedBoardId.value = boardId
        _currentPage.value = 1
        triggerRefresh()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
}
