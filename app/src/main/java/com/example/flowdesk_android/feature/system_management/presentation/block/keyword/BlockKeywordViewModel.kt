package com.example.flowdesk_android.feature.system_management.presentation.block.keyword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BlockWordItem
import com.example.flowdesk_android.feature.system_management.domain.model.BulkBlockWordResult
import com.example.flowdesk_android.feature.system_management.domain.repository.SecurityBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed class BlockWordUiState {
    object Loading : BlockWordUiState()
    data class Success(val items: List<BlockWordItem>, val totalCount: Int) : BlockWordUiState()
    data class Error(val message: String) : BlockWordUiState()
}

sealed class BlockWordDetailUiState {
    object Loading : BlockWordDetailUiState()
    data class Success(val item: BlockWordItem) : BlockWordDetailUiState()
    data class Error(val message: String) : BlockWordDetailUiState()
}

@HiltViewModel
class BlockKeywordViewModel @Inject constructor(
    private val repository: SecurityBlockRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<BlockWordDetailUiState>(BlockWordDetailUiState.Loading)
    val detailState: StateFlow<BlockWordDetailUiState> = _detailState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    private val allLoadedItems = mutableListOf<BlockWordItem>()
    private var totalPages = 1
    private var isPagingLoading = false

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BlockWordUiState> = combine(
        debouncedQuery,
        _currentPage,
        _refreshTrigger
    ) { query, page, _ ->
        query to page
    }.flatMapLatest { (query, page) ->
        flow {
            if (page == 1) {
                emit(BlockWordUiState.Loading)
            }
            val queryParam = query.ifBlank { null }
            repository.getBlockWords(
                page = page,
                limit = 20,
                q = queryParam
            ).fold(
                onSuccess = { response ->
                    totalPages = response.pageInfo.totalPages
                    if (page == 1) {
                        allLoadedItems.clear()
                    }
                    allLoadedItems.addAll(response.items)
                    emit(
                        BlockWordUiState.Success(
                            items = allLoadedItems.toList(),
                            totalCount = response.pageInfo.totalItems
                        )
                    )
                    isPagingLoading = false
                },
                onFailure = { err ->
                    emit(BlockWordUiState.Error(err.message ?: "금칙어 목록을 가져오는데 실패했습니다."))
                    isPagingLoading = false
                    _errorMessage.emit(err.message ?: "금칙어 목록 로드 실패")
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BlockWordUiState.Loading
    )

    init {
        // 검색어나 리프레시 트리거 발생 시 자동으로 첫 페이지로 리셋
        viewModelScope.launch {
            combine(debouncedQuery, _refreshTrigger) { _, _ -> }.collect {
                _currentPage.value = 1
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun loadMore() {
        val page = _currentPage.value
        if (page < totalPages && !isPagingLoading) {
            isPagingLoading = true
            _currentPage.value = page + 1
        }
    }

    fun addBlockWord(
        blockWord: String,
        matchType: String,
        reason: String,
        isActive: Int,
        onResult: (Result<BlockWordItem>) -> Unit
    ) {
        viewModelScope.launch {
            repository.createBlockWord(blockWord, matchType, reason, isActive)
                .onSuccess { item ->
                    triggerRefresh()
                    onResult(Result.success(item))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "금칙어 등록 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun addBulkBlockWord(
        words: String,
        matchType: String,
        reason: String,
        isActive: Int,
        onResult: (Result<BulkBlockWordResult>) -> Unit
    ) {
        viewModelScope.launch {
            repository.createBulkBlockWord(words, matchType, reason, isActive)
                .onSuccess { result ->
                    triggerRefresh()
                    onResult(Result.success(result))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "대량 금칙어 등록 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun updateBlockWord(
        id: Long,
        matchType: String,
        reason: String,
        isActive: Int,
        onResult: (Result<BlockWordItem>) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateBlockWord(id, matchType, reason, isActive)
                .onSuccess { item ->
                    triggerRefresh()
                    onResult(Result.success(item))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "금칙어 수정 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun loadDetail(id: Long) {
        _detailState.value = BlockWordDetailUiState.Loading
        viewModelScope.launch {
            repository.getBlockWordDetail(id)
                .onSuccess { item ->
                    _detailState.value = BlockWordDetailUiState.Success(item)
                }
                .onFailure { err ->
                    _detailState.value = BlockWordDetailUiState.Error(err.message ?: "상세 정보를 가져오는 데 실패했습니다.")
                }
        }
    }

    fun deleteBlockWord(id: Long, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            repository.deleteBlockWord(id)
                .onSuccess {
                    triggerRefresh()
                    onResult(Result.success(Unit))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "금칙어 해제 실패")
                    onResult(Result.failure(err))
                }
        }
    }
}
