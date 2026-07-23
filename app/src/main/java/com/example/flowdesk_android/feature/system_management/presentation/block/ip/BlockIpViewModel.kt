package com.example.flowdesk_android.feature.system_management.presentation.block.ip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BlockIpItem
import com.example.flowdesk_android.feature.system_management.domain.model.BulkBlockResult
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

sealed class BlockIpUiState {
    object Loading : BlockIpUiState()
    data class Success(
        val items: List<BlockIpItem>,
        val totalCount: Int,
        val canWrite: Boolean,
        val canUpdate: Boolean,
        val canDelete: Boolean
    ) : BlockIpUiState()
    data class Error(val message: String) : BlockIpUiState()
}

sealed class BlockIpDetailUiState {
    object Loading : BlockIpDetailUiState()
    data class Success(
        val item: BlockIpItem,
        val canUpdate: Boolean,
        val canDelete: Boolean
    ) : BlockIpDetailUiState()
    data class Error(val message: String) : BlockIpDetailUiState()
}

@HiltViewModel
class BlockIpViewModel @Inject constructor(
    private val repository: SecurityBlockRepository,
    private val sessionManager: com.example.flowdesk_android.data.local.SessionManager
) : ViewModel() {

    private val _detailLoading = MutableStateFlow(false)
    private val _detailError = MutableStateFlow<String?>(null)
    private val _loadedIpDetail = MutableStateFlow<BlockIpItem?>(null)

    val detailState: StateFlow<BlockIpDetailUiState> = combine(
        _detailLoading,
        _detailError,
        _loadedIpDetail,
        sessionManager.observePermission("security.update"),
        sessionManager.observePermission("security.delete")
    ) { loading, error, item, canUpdate, canDelete ->
        when {
            loading -> BlockIpDetailUiState.Loading
            error != null -> BlockIpDetailUiState.Error(error)
            item != null -> BlockIpDetailUiState.Success(item, canUpdate, canDelete)
            else -> BlockIpDetailUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BlockIpDetailUiState.Loading
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    private val allLoadedItems = mutableListOf<BlockIpItem>()
    private var totalPages = 1
    private var isPagingLoading = false

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _repoState = combine(
        debouncedQuery,
        _currentPage,
        _refreshTrigger
    ) { query, page, _ ->
        query to page
    }.flatMapLatest { (query, page) ->
        flow {
            if (page == 1) {
                emit(null)
            }
            val queryParam = query.ifBlank { null }
            repository.getBlockIps(
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
                    emit(Result.success(allLoadedItems.toList() to response.pageInfo.totalItems))
                    isPagingLoading = false
                },
                onFailure = { err ->
                    emit(Result.failure(err))
                    isPagingLoading = false
                    _errorMessage.emit(err.message ?: "IP 차단 목록 로드 실패")
                }
            )
        }
    }

    val uiState: StateFlow<BlockIpUiState> = combine(
        _repoState,
        sessionManager.observePermission("security.create"),
        sessionManager.observePermission("security.update"),
        sessionManager.observePermission("security.delete")
    ) { repoResult, canWrite, canUpdate, canDelete ->
        if (repoResult == null) {
            BlockIpUiState.Loading
        } else {
            repoResult.fold(
                onSuccess = { (items, total) ->
                    BlockIpUiState.Success(items, total, canWrite, canUpdate, canDelete)
                },
                onFailure = { err ->
                    BlockIpUiState.Error(err.message ?: "IP 차단 목록을 가져오는데 실패했습니다.")
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BlockIpUiState.Loading
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

    fun addBlockIp(ip: String, reason: String, isActive: Int, onResult: (Result<BlockIpItem>) -> Unit) {
        viewModelScope.launch {
            repository.createBlockIp(ip, reason, isActive)
                .onSuccess { item ->
                    triggerRefresh()
                    onResult(Result.success(item))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "IP 차단 등록 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun addBulkBlockIp(ips: String, reason: String, isActive: Int, onResult: (Result<BulkBlockResult>) -> Unit) {
        viewModelScope.launch {
            repository.createBulkBlockIp(ips, reason, isActive)
                .onSuccess { result ->
                    triggerRefresh()
                    onResult(Result.success(result))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "대량 IP 차단 등록 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun updateBlockIp(id: Long, reason: String, isActive: Int, onResult: (Result<BlockIpItem>) -> Unit) {
        viewModelScope.launch {
            repository.updateBlockIp(id, reason, isActive)
                .onSuccess { item ->
                    triggerRefresh()
                    onResult(Result.success(item))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "IP 차단 수정 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _detailLoading.value = true
            _detailError.value = null
            _loadedIpDetail.value = null
            repository.getBlockIpDetail(id)
                .onSuccess { item ->
                    _loadedIpDetail.value = item
                    _detailLoading.value = false
                }
                .onFailure { err ->
                    _detailError.value = err.message ?: "상세 정보를 가져오는 데 실패했습니다."
                    _detailLoading.value = false
                }
        }
    }

    fun deleteBlockIp(id: Long, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            repository.deleteBlockIp(id)
                .onSuccess {
                    triggerRefresh()
                    onResult(Result.success(Unit))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "IP 차단 해제 실패")
                    onResult(Result.failure(err))
                }
        }
    }
}
