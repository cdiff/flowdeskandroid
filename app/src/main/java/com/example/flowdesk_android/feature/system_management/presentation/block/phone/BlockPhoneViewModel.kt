package com.example.flowdesk_android.feature.system_management.presentation.block.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.BlockPhoneItem
import com.example.flowdesk_android.feature.system_management.domain.model.BulkBlockPhoneResult
import com.example.flowdesk_android.feature.system_management.domain.repository.SecurityBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

@OptIn(FlowPreview::class)
@HiltViewModel
class BlockPhoneViewModel @Inject constructor(
    private val repository: SecurityBlockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockPhoneUiState>(BlockPhoneUiState.Loading)
    val uiState: StateFlow<BlockPhoneUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<BlockPhoneDetailUiState>(BlockPhoneDetailUiState.Loading)
    val detailState: StateFlow<BlockPhoneDetailUiState> = _detailState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allLoadedItems = mutableListOf<BlockPhoneItem>()
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
                    loadBlockPhones(isRefresh = true)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadBlockPhones(isRefresh: Boolean = false) {
        if (isPagingLoading) return

        if (isRefresh) {
            currentPage = 1
            totalPages = 1
            allLoadedItems.clear()
            _uiState.value = BlockPhoneUiState.Loading
        } else {
            if (currentPage >= totalPages) return
            isPagingLoading = true
        }

        viewModelScope.launch {
            val queryParam = _searchQuery.value.ifBlank { null }
            repository.getBlockPhones(
                page = currentPage,
                limit = 20,
                q = queryParam
            ).onSuccess { response ->
                totalPages = response.pageInfo.totalPages
                allLoadedItems.addAll(response.items)

                _uiState.value = BlockPhoneUiState.Success(
                    items = allLoadedItems.toList(),
                    totalCount = response.pageInfo.totalItems
                )
                isPagingLoading = false
            }.onFailure { err ->
                _uiState.value = BlockPhoneUiState.Error(err.message ?: "휴대폰 차단 목록을 가져오는데 실패했습니다.")
                isPagingLoading = false
                _errorMessage.emit(err.message ?: "휴대폰 차단 목록 로드 실패")
            }
        }
    }

    fun loadMore() {
        if (currentPage < totalPages && !isPagingLoading) {
            currentPage++
            loadBlockPhones(isRefresh = false)
        }
    }

    fun addBlockPhone(blockHp: String, reason: String, isActive: Int, onResult: (Result<BlockPhoneItem>) -> Unit) {
        viewModelScope.launch {
            repository.createBlockPhone(blockHp, reason, isActive)
                .onSuccess { item ->
                    loadBlockPhones(isRefresh = true)
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
                    loadBlockPhones(isRefresh = true)
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
                    loadBlockPhones(isRefresh = true)
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
                    loadBlockPhones(isRefresh = true)
                    onResult(Result.success(Unit))
                }
                .onFailure { err ->
                    _errorMessage.emit(err.message ?: "휴대폰 차단 해제 실패")
                    onResult(Result.failure(err))
                }
        }
    }
}
