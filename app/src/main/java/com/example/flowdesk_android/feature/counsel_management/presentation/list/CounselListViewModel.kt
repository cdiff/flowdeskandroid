package com.example.flowdesk_android.feature.counsel_management.presentation.list

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselItem
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselList
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.TopWebsite
import com.example.flowdesk_android.feature.counsel_management.domain.repository.CounselRepository
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed class CounselListUiState {
    object Loading : CounselListUiState()
    data class Success(val items: List<CounselItem>, val totalCount: Int) : CounselListUiState()
    data class Error(val message: String) : CounselListUiState()
}

data class CounselFilterState(
    val q: String? = null,
    val counselStat: Int? = null,
    val empSeq: Int? = null,
    val webCode: String? = null,
    val duplicateState: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val resvStartDate: String? = null,
    val resvEndDate: String? = null
)

@HiltViewModel
class CounselListViewModel @Inject constructor(
    private val counselRepository: CounselRepository
) : BaseViewModel() {

    private val _statusCounts = MutableStateFlow<List<CounselStatusStat>>(emptyList())
    val statusCounts: StateFlow<List<CounselStatusStat>> = _statusCounts.asStateFlow()

    private val _employeeList = MutableStateFlow<List<EmployeeStat>>(emptyList())
    val employeeList: StateFlow<List<EmployeeStat>> = _employeeList.asStateFlow()

    private val _websiteList = MutableStateFlow<List<TopWebsite>>(emptyList())
    val websiteList: StateFlow<List<TopWebsite>> = _websiteList.asStateFlow()

    // 디바운스 적용을 위해 순수 검색어와 나머지 필터를 나눔
    private val _searchQuery = MutableStateFlow<String?>(null)
    private val _filtersWithoutQuery = MutableStateFlow(CounselFilterState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 최종 결합된 필터 상태
    val filterState: StateFlow<CounselFilterState> = combine(
        debouncedQuery,
        _filtersWithoutQuery
    ) { query, filters ->
        filters.copy(q = query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CounselFilterState())

    // 페이징 처리를 위한 StateFlow
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val allLoadedItems = mutableListOf<CounselItem>()
    private var totalPages = 1
    private var isPagingLoading = false

    // 선언형 UI 상태 파이프라인
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CounselListUiState> = combine(
        filterState,
        _currentPage
    ) { filter, page ->
        filter to page
    }.flatMapLatest { (filter, page) ->
        flow {
            if (page == 1) {
                emit(CounselListUiState.Loading)
            }
            
            val isUnassignedQuery = filter.q?.trim() == "미배정"
            val queryParam = if (isUnassignedQuery) null else filter.q
            val empSeqParam = if (isUnassignedQuery) 0 else filter.empSeq

            counselRepository.getCounsels(
                page = page,
                limit = 20,
                query = queryParam,
                counselStat = filter.counselStat,
                empSeq = empSeqParam,
                webCode = filter.webCode,
                startDate = filter.startDate,
                endDate = filter.endDate,
                duplicateState = filter.duplicateState,
                resvStartDate = filter.resvStartDate,
                resvEndDate = filter.resvEndDate
            ).fold(
                onSuccess = { list ->
                    totalPages = list.pageInfo?.totalPages ?: 1
                    if (page == 1) {
                        allLoadedItems.clear()
                    }
                    allLoadedItems.addAll(list.items)
                    emit(
                        CounselListUiState.Success(
                            items = allLoadedItems.toList(),
                            totalCount = list.pageInfo?.totalItems ?: allLoadedItems.size
                        )
                    )
                    isPagingLoading = false
                },
                onFailure = { err ->
                    emit(CounselListUiState.Error(err.message ?: "상담 목록 조회에 실패했습니다."))
                    isPagingLoading = false
                    sendError(err.message ?: "상담 목록 로딩 실패")
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CounselListUiState.Loading
    )

    init {
        // 필터가 바뀌면 자동으로 페이지를 1로 리셋하고 대시보드 통계를 업데이트합니다.
        viewModelScope.launch {
            filterState.collectLatest { filter ->
                _currentPage.value = 1
                fetchStatusCounts(filter)
            }
        }
    }

    fun loadMore() {
        val page = _currentPage.value
        if (page < totalPages && !isPagingLoading) {
            isPagingLoading = true
            _currentPage.value = page + 1
        }
    }

    // 수동 리프레시 트리거용 Flow
    private val _refreshTrigger = MutableStateFlow(0)

    // 선언형 UI 상태 파이프라인
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CounselListUiState> = combine(
        filterState,
        _currentPage,
        _refreshTrigger
    ) { filter, page, _ ->
        filter to page
    }.flatMapLatest { (filter, page) ->
        flow {
            if (page == 1) {
                emit(CounselListUiState.Loading)
            }
            
            val isUnassignedQuery = filter.q?.trim() == "미배정"
            val queryParam = if (isUnassignedQuery) null else filter.q
            val empSeqParam = if (isUnassignedQuery) 0 else filter.empSeq

            counselRepository.getCounsels(
                page = page,
                limit = 20,
                query = queryParam,
                counselStat = filter.counselStat,
                empSeq = empSeqParam,
                webCode = filter.webCode,
                startDate = filter.startDate,
                endDate = filter.endDate,
                duplicateState = filter.duplicateState,
                resvStartDate = filter.resvStartDate,
                resvEndDate = filter.resvEndDate
            ).fold(
                onSuccess = { list ->
                    totalPages = list.pageInfo?.totalPages ?: 1
                    if (page == 1) {
                        allLoadedItems.clear()
                    }
                    allLoadedItems.addAll(list.items)
                    emit(
                        CounselListUiState.Success(
                            items = allLoadedItems.toList(),
                            totalCount = list.pageInfo?.totalItems ?: allLoadedItems.size
                        )
                    )
                    isPagingLoading = false
                },
                onFailure = { err ->
                    emit(CounselListUiState.Error(err.message ?: "상담 목록 조회에 실패했습니다."))
                    isPagingLoading = false
                    sendError(err.message ?: "상담 목록 로딩 실패")
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CounselListUiState.Loading
    )

    init {
        // 필터가 바뀌면 자동으로 페이지를 1로 리셋하고 대시보드 통계를 업데이트합니다.
        viewModelScope.launch {
            filterState.collectLatest { filter ->
                _currentPage.value = 1
                fetchStatusCounts(filter)
            }
        }
    }

    fun loadMore() {
        val page = _currentPage.value
        if (page < totalPages && !isPagingLoading) {
            isPagingLoading = true
            _currentPage.value = page + 1
        }
    }

    fun fetchStatusCounts(filter: CounselFilterState = filterState.value) {
        viewModelScope.launch {
            counselRepository.getDashboard(filter.startDate, filter.endDate)
                .onSuccess { dashboard ->
                    _statusCounts.value = dashboard.statusDistribution
                    _employeeList.value = dashboard.employeeStats
                    _websiteList.value = dashboard.topWebsites
                }
                .onFailure {
                    // Fail silently
                }
        }
    }

    fun updateStatusFilter(counselStat: Int?) {
        _filtersWithoutQuery.update { it.copy(counselStat = counselStat) }
    }

    fun updateSearchQuery(q: String?) {
        val queryText = if (q.isNullOrBlank()) null else q.trim()
        _searchQuery.value = queryText
    }

    fun updateDateFilter(start: String?, end: String?) {
        _filtersWithoutQuery.update { it.copy(startDate = start, endDate = end) }
    }

    fun updateManagerFilter(empSeq: Int?) {
        _filtersWithoutQuery.update { it.copy(empSeq = empSeq) }
    }

    fun updateWebsiteFilter(webCode: String?) {
        _filtersWithoutQuery.update { it.copy(webCode = webCode) }
    }

    fun updateDuplicateFilter(duplicateState: String?) {
        _filtersWithoutQuery.update { it.copy(duplicateState = duplicateState) }
    }

    fun clearFilters() {
        _searchQuery.value = null
        _filtersWithoutQuery.value = CounselFilterState()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun updateCounselInfo(id: Int, name: String, counselHp: String, counselMemo: String?) {
        viewModelScope.launch {
            val request = CounselUpdateRequest(
                name = name,
                counselHp = counselHp,
                counselMemo = counselMemo
            )
            counselRepository.updateCounsel(id, request)
                .onSuccess {
                    triggerRefresh()
                }
                .onFailure { err ->
                    sendError(err.message ?: "상담 수정 실패")
                }
        }
    }

    fun deleteCounsel(id: Int) {
        viewModelScope.launch {
            counselRepository.deleteCounsel(id)
                .onSuccess {
                    triggerRefresh()
                }
                .onFailure { err ->
                    sendError(err.message ?: "상담 삭제 실패")
                }
        }
    }
}
