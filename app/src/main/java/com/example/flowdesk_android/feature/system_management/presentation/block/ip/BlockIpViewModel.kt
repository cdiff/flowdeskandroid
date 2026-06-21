package com.example.flowdesk_android.feature.system_management.presentation.block.ip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BlockIpItem
import com.example.flowdesk_android.feature.system_management.domain.model.BulkBlockResult
import com.example.flowdesk_android.feature.system_management.domain.repository.SecurityBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BlockIpUiState {
    object Loading : BlockIpUiState()
    data class Success(val items: List<BlockIpItem>, val totalCount: Int) : BlockIpUiState()
    data class Error(val message: String) : BlockIpUiState()
}

sealed class BlockIpDetailUiState {
    object Loading : BlockIpDetailUiState()
    data class Success(val item: BlockIpItem) : BlockIpDetailUiState()
    data class Error(val message: String) : BlockIpDetailUiState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class BlockIpViewModel @Inject constructor(
    private val repository: SecurityBlockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockIpUiState>(BlockIpUiState.Loading)
    val uiState: StateFlow<BlockIpUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<BlockIpDetailUiState>(BlockIpDetailUiState.Loading)
    val detailState: StateFlow<BlockIpDetailUiState> = _detailState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allLoadedItems = mutableListOf<BlockIpItem>()
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
                    loadBlockIps(isRefresh = true)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private var fetchJob: kotlinx.coroutines.Job? = null

    fun loadBlockIps(isRefresh: Boolean = false) {
        if (isRefresh) {
            fetchJob?.cancel()
            isPagingLoading = false
            currentPage = 1
            totalPages = 1
            allLoadedItems.clear()
            _uiState.value = BlockIpUiState.Loading
        } else {
            if (isPagingLoading || currentPage >= totalPages) return
            isPagingLoading = true
        }

        fetchJob = viewModelScope.launch {
            val queryParam = _searchQuery.value.ifBlank { null }
            repository.getBlockIps(
                page = currentPage,
                limit = 20,
                q = queryParam
            ).onSuccess { response ->
                totalPages = response.pageInfo.totalPages
                allLoadedItems.addAll(response.items)

                _uiState.value = BlockIpUiState.Success(
                    items = allLoadedItems.toList(),
                    totalCount = response.pageInfo.totalItems
                )
                isPagingLoading = false
            }.onFailure { err ->
                if (err is kotlinx.coroutines.CancellationException) return@onFailure
                _uiState.value = BlockIpUiState.Error(err.message ?: "IP 차단 목록을 가져오는데 실패했습니다.")
                isPagingLoading = false
                _errorMessage.emit(err.message ?: "IP 차단 목록 로드 실패")
            }
        }
    }

    fun loadMore() {
        if (currentPage < totalPages && !isPagingLoading) {
            currentPage++
            loadBlockIps(isRefresh = false)
        }
    }

    fun addBlockIp(ip: String, reason: String, isActive: Int, onResult: (Result<BlockIpItem>) -> Unit) {
        viewModelScope.launch {
            repository.createBlockIp(ip, reason, isActive)
                .onSuccess { item ->
                    loadBlockIps(isRefresh = true)
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
                    loadBlockIps(isRefresh = true)
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
                    loadBlockIps(isRefresh = true)
                    onResult(Result.success(item))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "IP 차단 수정 실패")
                    onResult(Result.failure(err))
                }
        }
    }

    fun loadDetail(id: Long) {
        _detailState.value = BlockIpDetailUiState.Loading
        viewModelScope.launch {
            repository.getBlockIpDetail(id)
                .onSuccess { item ->
                    _detailState.value = BlockIpDetailUiState.Success(item)
                }
                .onFailure { err ->
                    _detailState.value = BlockIpDetailUiState.Error(err.message ?: "상세 정보를 가져오는 데 실패했습니다.")
                }
        }
    }

    fun deleteBlockIp(id: Long, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            repository.deleteBlockIp(id)
                .onSuccess {
                    loadBlockIps(isRefresh = true)
                    onResult(Result.success(Unit))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "IP 차단 해제 실패")
                    onResult(Result.failure(err))
                }
        }
    }
}
