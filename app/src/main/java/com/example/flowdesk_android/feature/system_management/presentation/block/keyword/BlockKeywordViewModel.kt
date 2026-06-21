package com.example.flowdesk_android.feature.system_management.presentation.block.keyword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BlockWordItem
import com.example.flowdesk_android.feature.system_management.domain.model.BulkBlockWordResult
import com.example.flowdesk_android.feature.system_management.domain.repository.SecurityBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

@OptIn(FlowPreview::class)
@HiltViewModel
class BlockKeywordViewModel @Inject constructor(
    private val repository: SecurityBlockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockWordUiState>(BlockWordUiState.Loading)
    val uiState: StateFlow<BlockWordUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<BlockWordDetailUiState>(BlockWordDetailUiState.Loading)
    val detailState: StateFlow<BlockWordDetailUiState> = _detailState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allLoadedItems = mutableListOf<BlockWordItem>()
    private var currentPage = 1
    private var totalPages = 1
    private var isPagingLoading = false

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .collect { _ ->
                    loadBlockWords(isRefresh = true)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private var fetchJob: kotlinx.coroutines.Job? = null

    fun loadBlockWords(isRefresh: Boolean = false) {
        if (isRefresh) {
            fetchJob?.cancel()
            isPagingLoading = false
            currentPage = 1
            totalPages = 1
            allLoadedItems.clear()
            _uiState.value = BlockWordUiState.Loading
        } else {
            if (isPagingLoading || currentPage >= totalPages) return
            isPagingLoading = true
        }

        fetchJob = viewModelScope.launch {
            val queryParam = _searchQuery.value.ifBlank { null }
            repository.getBlockWords(
                page = currentPage,
                limit = 20,
                q = queryParam
            ).onSuccess { response ->
                totalPages = response.pageInfo.totalPages
                allLoadedItems.addAll(response.items)

                _uiState.value = BlockWordUiState.Success(
                    items = allLoadedItems.toList(),
                    totalCount = response.pageInfo.totalItems
                )
                isPagingLoading = false
            }.onFailure { err ->
                if (err is kotlinx.coroutines.CancellationException) return@onFailure
                _uiState.value = BlockWordUiState.Error(err.message ?: "금칙어 목록을 가져오는데 실패했습니다.")
                isPagingLoading = false
                _errorMessage.emit(err.message ?: "금칙어 목록 로드 실패")
            }
        }
    }

    fun loadMore() {
        if (currentPage < totalPages && !isPagingLoading) {
            currentPage++
            loadBlockWords(isRefresh = false)
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
                    loadBlockWords(isRefresh = true)
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
                    loadBlockWords(isRefresh = true)
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
                    loadBlockWords(isRefresh = true)
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
                    loadBlockWords(isRefresh = true)
                    onResult(Result.success(Unit))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "금칙어 해제 실패")
                    onResult(Result.failure(err))
                }
        }
    }
}
