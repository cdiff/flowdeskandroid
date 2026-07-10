package com.example.flowdesk_android.feature.content_management.presentation.board_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.local.SessionManager
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class BoardPostListViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    private val sessionManager: SessionManager
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
        combine(
            _selectedBoardId,
            _currentPage,
            _refreshTrigger.onStart { emit(Unit) }
        ) { boardId, page, _ -> Triple(boardId, page, _searchQuery.value) }
            .flatMapLatest { (boardId, page, query) ->
                flow {
                    if (_boardTypes.value.isEmpty()) {
                        emit(null)  // Loading 신호
                        boardRepository.getBoardTypes()
                            .onSuccess { types ->
                                val activeTypes = types.filter { it.isActive }
                                val allBoardType = BoardType(
                                    boardId = 0L,
                                    boardKey = "all",
                                    name = "전체",
                                    description = "전체 게시글 조회",
                                    isActive = true,
                                    sortOrder = 0,
                                    createdAt = null,
                                    updatedAt = null
                                )
                                val typesWithAll = listOf(allBoardType) + activeTypes
                                _boardTypes.value = typesWithAll
                                if (typesWithAll.isNotEmpty() && _selectedBoardId.value == -1L) {
                                    _selectedBoardId.value = 0L // 기본값으로 '전체' 선택
                                }
                            }
                            .onFailure { err ->
                                emit(Result.failure<Pair<List<BoardPost>, Int>>(err))
                                return@flow
                            }
                    }

                    val currentBoardId = _selectedBoardId.value
                    if (currentBoardId == -1L) {
                        emit(Result.success(emptyList<BoardPost>() to 0))
                        return@flow
                    }

                    if (currentBoardId == 0L) {
                        // '전체' 탭 선택 시 활성화된 모든 게시판의 글을 호출하여 병합
                        val activeBoardsOnly = _boardTypes.value.filter { it.boardId != 0L }
                        if (activeBoardsOnly.isEmpty()) {
                            emit(Result.success(emptyList<BoardPost>() to 0))
                            return@flow
                        }

                        val allPosts = mutableListOf<BoardPost>()
                        var aggregateTotalItems = 0
                        var hasError = false
                        var lastError: Throwable? = null

                        kotlinx.coroutines.coroutineScope {
                            val deferreds = activeBoardsOnly.map { board ->
                                async(kotlinx.coroutines.Dispatchers.IO) {
                                    boardRepository.getBoardPosts(board.boardId, page, 20, query.ifEmpty { null }) to board.name
                                }
                            }

                            deferreds.forEach { deferred ->
                                try {
                                    val (result, boardName) = deferred.await()
                                    result.fold(
                                        onSuccess = { (posts, pageInfo) ->
                                            val mapped = posts.map { it.copy(boardName = boardName) }
                                            allPosts.addAll(mapped)
                                            aggregateTotalItems += pageInfo.totalItems
                                        },
                                        onFailure = { err ->
                                            hasError = true
                                            lastError = err
                                        }
                                    )
                                } catch (e: Exception) {
                                    hasError = true
                                    lastError = e
                                }
                            }
                        }

                        if (hasError && allPosts.isEmpty()) {
                            emit(Result.failure(lastError ?: Exception("전체 게시글을 불러오지 못했습니다.")))
                        } else {
                            // 공지 상단 고정 및 작성일시 내림차순 정렬
                            val sortedPosts = allPosts.sortedWith(
                                compareByDescending<BoardPost> { it.isNotice }
                                    .thenByDescending { it.createdAt }
                            )
                            emit(Result.success(sortedPosts to aggregateTotalItems))
                        }
                        return@flow
                    }

                    // 특정 게시판 탭 조회
                    boardRepository.getBoardPosts(currentBoardId, page, 20, query.ifEmpty { null })
                        .onSuccess { (posts, pageInfo) ->
                            val sortedPosts = posts.sortedWith(compareByDescending<BoardPost> { it.isNotice }.thenByDescending { it.createdAt })
                            val currentBoardName = _boardTypes.value.find { it.boardId == currentBoardId }?.name
                            val mappedPosts = sortedPosts.map { it.copy(boardName = currentBoardName) }
                            emit(Result.success(mappedPosts to pageInfo.totalItems))
                        }
                        .onFailure { err ->
                            emit(Result.failure<Pair<List<BoardPost>, Int>>(err))
                        }
                }
            },
        sessionManager.observePermission("boards.posts.create")
    ) { repoResult, canWrite ->
        when {
            repoResult == null -> BoardPostListUiState.Loading
            repoResult.isFailure -> BoardPostListUiState.Error(
                repoResult.exceptionOrNull()?.message ?: "오류가 발생했습니다."
            )
            else -> {
                val (posts, total) = repoResult.getOrThrow()
                BoardPostListUiState.Success(_boardTypes.value, posts, total, canWrite)
            }
        }
    }.stateIn(
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
