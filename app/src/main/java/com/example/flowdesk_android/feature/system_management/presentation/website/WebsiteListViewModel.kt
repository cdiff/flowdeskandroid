package com.example.flowdesk_android.feature.system_management.presentation.website

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.Website
import com.example.flowdesk_android.feature.system_management.domain.repository.WebsiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteListViewModel @Inject constructor(
    private val repository: WebsiteRepository,
    private val sessionManager: com.example.flowdesk_android.data.local.SessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    private val _loadedWebsites = MutableStateFlow<List<Website>>(emptyList())
    val loadedWebsites = _loadedWebsites.asStateFlow()

    private val _totalItemsCount = MutableStateFlow(0)
    val totalWebsitesCount = _totalItemsCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    // 통계 정보 실시간 유도 (Toss 수평 통계 바 연동)
    val activeWebsitesCount = _loadedWebsites.map { list ->
        list.count { it.isActive }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inactiveWebsitesCount = _loadedWebsites.map { list ->
        list.count { !it.isActive }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val permissionFlow = combine(
        sessionManager.observePermission("websites.create"),
        sessionManager.observePermission("websites.update"),
        sessionManager.observePermission("websites.delete")
    ) { canWrite, canUpdate, canDelete ->
        Triple(canWrite, canUpdate, canDelete)
    }

    // UI 상태 흐름 정의 (Mutable 데이터 변화와 로딩 여부를 즉시 방출)
    val uiState: StateFlow<WebsiteListUiState> = combine(
        _loadedWebsites,
        _totalItemsCount,
        _isLoading,
        permissionFlow
    ) { websites, totalCount, loading, permissions ->
        val (canWrite, canUpdate, canDelete) = permissions
        if (websites.isEmpty() && loading) {
            WebsiteListUiState.Loading
        } else {
            WebsiteListUiState.Success(websites, totalCount, canWrite, canUpdate, canDelete)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WebsiteListUiState.Loading
    )

    init {
        // 검색어, 페이지, 새로고침 트리거 변경 시 자동으로 API 호출하여 데이터 로드
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300).distinctUntilChanged(),
                _currentPage,
                _refreshTrigger
            ) { query, page, _ ->
                query to page
            }.collectLatest { (query, page) ->
                _isLoading.value = true
                val result = repository.getWebsites(page, 20, query)
                _isLoading.value = false
                
                result.fold(
                    onSuccess = { response ->
                        if (page == 1) {
                            _loadedWebsites.value = response.items
                        } else {
                            // 무한 스크롤 추가 로드
                            _loadedWebsites.value = _loadedWebsites.value + response.items
                        }
                        _totalItemsCount.value = response.pageInfo.totalItems
                    },
                    onFailure = { throwable ->
                        _errorMessage.emit(throwable.message ?: "웹사이트 목록을 불러오지 못했습니다.")
                    }
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1 // 검색어 변경 시 페이지 초기화
    }

    fun loadNextPage() {
        if (_isLoading.value) return
        val currentLoadedCount = _loadedWebsites.value.size
        if (currentLoadedCount >= _totalItemsCount.value) return // 더 이상 데이터 없음
        
        _currentPage.value = _currentPage.value + 1
    }

    fun refresh() {
        _currentPage.value = 1
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    /**
     * 상태 즉시 토글 업데이트
     * API 통신 후 로컬 목록 데이터(_loadedWebsites)를 실시간 갱신하여 
     * 딜레이 없는 화면 업데이트 제공 (네트워크 리프레시 호출 생략)
     */
    fun updateWebsiteStatus(webCode: String, isActive: Boolean) {
        viewModelScope.launch {
            // 로딩 표시는 하되 전체화면 스피너(Loading) 상태 방출은 스킵함 (UX 향상)
            _isLoading.value = true
            repository.updateWebsiteStatus(webCode, isActive).fold(
                onSuccess = { updatedWebsite ->
                    // 리스트 상태 즉각 동기화 (DiffUtil이 작동하여 화면 딜레이 없음)
                    _loadedWebsites.value = _loadedWebsites.value.map { item ->
                        if (item.webCode == webCode) {
                            item.copy(isActive = updatedWebsite.isActive)
                        } else {
                            item
                        }
                    }
                    _isLoading.value = false
                },
                onFailure = { throwable ->
                    _isLoading.value = false
                    _errorMessage.emit(throwable.message ?: "상태 업데이트에 실패했습니다.")
                }
            )
        }
    }

    fun deleteWebsite(webCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteWebsite(webCode).fold(
                onSuccess = {
                    _isLoading.value = false
                    _errorMessage.emit("웹사이트가 삭제되었습니다.")
                    refresh() // 삭제 시에는 전체 리스트를 서버와 동기화
                },
                onFailure = { throwable ->
                    _isLoading.value = false
                    _errorMessage.emit(throwable.message ?: "삭제에 실패했습니다.")
                }
            )
        }
    }
}
