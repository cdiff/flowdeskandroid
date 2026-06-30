package com.example.flowdesk_android.feature.system_management.presentation.block.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BlockPhoneItem
import com.example.flowdesk_android.feature.system_management.domain.model.BulkBlockPhoneResult
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

sealed class BlockPhoneUiState {
    object Loading : BlockPhoneUiState()
    data class Success(val items: List<BlockPhoneItem>, val totalCount: Int) : BlockPhoneUiState()
    data class Error(val message: String) : BlockPhoneUiState()
}

sealed class BlockPhoneDetailUiState {
    object Loading : BlockPhoneDetailUiState()
    data class Success(val item: BlockPhoneItem) : BlockPhoneDetailUiState()
    data class Error(val message: String) : BlockPhoneDetailUiState()
}

@HiltViewModel
class BlockPhoneViewModel @Inject constructor(
    private val repository: SecurityBlockRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<BlockPhoneDetailUiState>(BlockPhoneDetailUiState.Loading)
    val detailState: StateFlow<BlockPhoneDetailUiState> = _detailState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    private val allLoadedItems = mutableListOf<BlockPhoneItem>()
    private var totalPages = 1
    private var isPagingLoading = false

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BlockPhoneUiState> = combine(
        debouncedQuery,
        _currentPage,
        _refreshTrigger
    ) { query, page, _ ->
        query to page
    }.flatMapLatest { (query, page) ->
        flow {
            if (page == 1) {
                emit(BlockPhoneUiState.Loading)
            }
            val queryParam = query.ifBlank { null }
            repository.getBlockPhones(
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
                        BlockPhoneUiState.Success(
                            items = allLoadedItems.toList(),
                            totalCount = response.pageInfo.totalItems
                        )
                    )
                    isPagingLoading = false
                },
                onFailure = { err ->
                    emit(BlockPhoneUiState.Error(err.message ?: "휴대폰 차단 목록을 가져오는데 실패했습니다."))
                    isPagingLoading = false
                    _errorMessage.emit(err.message ?: "휴대폰 차단 목록 로드 실패")
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BlockPhoneUiState.Loading
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

    fun addBlockPhone(blockHp: String, reason: String, isActive: Int, onResult: (Result<BlockPhoneItem>) -> Unit) {
        viewModelScope.launch {
            repository.createBlockPhone(blockHp, reason, isActive)
                .onSuccess { item ->
                    triggerRefresh()
                    onResult(Result.success(item))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "휴대폰 차단 등록 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun addBulkBlockPhone(phones: String, reason: String, isActive: Int, onResult: (Result<BulkBlockPhoneResult>) -> Unit) {
        viewModelScope.launch {
            repository.createBulkBlockPhone(phones, reason, isActive)
                .onSuccess { result ->
                    triggerRefresh()
                    onResult(Result.success(result))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "대량 휴대폰 차단 등록 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun updateBlockPhone(id: Long, reason: String, isActive: Int, onResult: (Result<BlockPhoneItem>) -> Unit) {
        viewModelScope.launch {
            repository.updateBlockPhone(id, reason, isActive)
                .onSuccess { item ->
                    triggerRefresh()
                    onResult(Result.success(item))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "휴대폰 차단 수정 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun loadDetail(id: Long) {
        _detailState.value = BlockPhoneDetailUiState.Loading
        viewModelScope.launch {
            repository.getBlockPhoneDetail(id)
                .onSuccess { item ->
                    _detailState.value = BlockPhoneDetailUiState.Success(item)
                }
                .onFailure { err ->
                    _detailState.value = BlockPhoneDetailUiState.Error(err.message ?: "상세 정보를 가져오는 데 실패했습니다.")
                }
        }
    }

    fun deleteBlockPhone(id: Long, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            repository.deleteBlockPhone(id)
                .onSuccess {
                    triggerRefresh()
                    onResult(Result.success(Unit))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "휴대폰 차단 해제 실패")
                    onResult(Result.failure(err))
                }
        }
    }
}
