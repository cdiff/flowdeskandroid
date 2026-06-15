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
import kotlinx.coroutines.launch
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

    private val _uiState = MutableStateFlow<CounselListUiState>(CounselListUiState.Loading)
    val uiState: StateFlow<CounselListUiState> = _uiState.asStateFlow()

    private val _statusCounts = MutableStateFlow<List<CounselStatusStat>>(emptyList())
    val statusCounts: StateFlow<List<CounselStatusStat>> = _statusCounts.asStateFlow()

    private val _employeeList = MutableStateFlow<List<EmployeeStat>>(emptyList())
    val employeeList: StateFlow<List<EmployeeStat>> = _employeeList.asStateFlow()

    private val _websiteList = MutableStateFlow<List<TopWebsite>>(emptyList())
    val websiteList: StateFlow<List<TopWebsite>> = _websiteList.asStateFlow()

    private val _filterState = MutableStateFlow(CounselFilterState())
    val filterState: StateFlow<CounselFilterState> = _filterState.asStateFlow()

    private val allLoadedItems = mutableListOf<CounselItem>()
    private var currentPage = 1
    private var totalPages = 1
    private var isPagingLoading = false

    init {
        // refreshAll()은 Fragment의 onViewCreated에서 호출하여 뷰가 그려질 때마다 최신화합니다.
    }

    fun refreshAll() {
        fetchStatusCounts()
        loadCounsels(isRefresh = true)
    }

    fun loadCounsels(isRefresh: Boolean = false) {
        if (isPagingLoading) return

        if (isRefresh) {
            currentPage = 1
            totalPages = 1
            allLoadedItems.clear()
            _uiState.value = CounselListUiState.Loading
        } else {
            if (currentPage >= totalPages) return
            isPagingLoading = true
        }

        viewModelScope.launch {
            val filter = _filterState.value
            counselRepository.getCounsels(
                page = currentPage,
                limit = 20,
                query = filter.q,
                counselStat = filter.counselStat,
                empSeq = filter.empSeq,
                webCode = filter.webCode,
                startDate = filter.startDate,
                endDate = filter.endDate,
                duplicateState = filter.duplicateState,
                resvStartDate = filter.resvStartDate,
                resvEndDate = filter.resvEndDate
            ).onSuccess { list ->
                totalPages = list.pageInfo?.totalPages ?: 1
                val items = list.items
                allLoadedItems.addAll(items)
                
                _uiState.value = CounselListUiState.Success(
                    items = allLoadedItems.toList(),
                    totalCount = list.pageInfo?.totalItems ?: allLoadedItems.size
                )
                
                isPagingLoading = false
            }.onFailure { err ->
                _uiState.value = CounselListUiState.Error(err.message ?: "상담 목록 조회에 실패했습니다.")
                isPagingLoading = false
                sendError(err.message ?: "상담 목록 로딩 실패")
            }
        }
    }

    fun loadMore() {
        if (currentPage < totalPages && !isPagingLoading) {
            currentPage++
            loadCounsels(isRefresh = false)
        }
    }

    fun fetchStatusCounts() {
        viewModelScope.launch {
            val filter = _filterState.value
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
        val current = _filterState.value
        if (current.counselStat != counselStat) {
            _filterState.value = current.copy(counselStat = counselStat)
            loadCounsels(isRefresh = true)
        }
    }

    fun updateSearchQuery(q: String?) {
        val current = _filterState.value
        val queryText = if (q.isNullOrBlank()) null else q.trim()
        if (current.q != queryText) {
            _filterState.value = current.copy(q = queryText)
            loadCounsels(isRefresh = true)
        }
    }

    fun updateDateFilter(start: String?, end: String?) {
        val current = _filterState.value
        if (current.startDate != start || current.endDate != end) {
            _filterState.value = current.copy(startDate = start, endDate = end)
            refreshAll()
        }
    }

    fun updateManagerFilter(empSeq: Int?) {
        val current = _filterState.value
        if (current.empSeq != empSeq) {
            _filterState.value = current.copy(empSeq = empSeq)
            loadCounsels(isRefresh = true)
        }
    }

    fun updateWebsiteFilter(webCode: String?) {
        val current = _filterState.value
        if (current.webCode != webCode) {
            _filterState.value = current.copy(webCode = webCode)
            loadCounsels(isRefresh = true)
        }
    }

    fun updateDuplicateFilter(duplicateState: String?) {
        val current = _filterState.value
        if (current.duplicateState != duplicateState) {
            _filterState.value = current.copy(duplicateState = duplicateState)
            loadCounsels(isRefresh = true)
        }
    }

    fun clearFilters() {
        _filterState.value = CounselFilterState()
        refreshAll()
    }

    fun updateCounselInfo(id: Int, name: String, counselHp: String, counselMemo: String?) {
        _uiState.value = CounselListUiState.Loading
        viewModelScope.launch {
            val request = CounselUpdateRequest(
                name = name,
                counselHp = counselHp,
                counselMemo = counselMemo
            )
            counselRepository.updateCounsel(id, request)
                .onSuccess {
                    refreshAll()
                }
                .onFailure { err ->
                    _uiState.value = CounselListUiState.Error(err.message ?: "상담 수정에 실패했습니다.")
                    sendError(err.message ?: "상담 수정 실패")
                }
        }
    }

    fun deleteCounsel(id: Int) {
        _uiState.value = CounselListUiState.Loading
        viewModelScope.launch {
            counselRepository.deleteCounsel(id)
                .onSuccess {
                    refreshAll()
                }
                .onFailure { err ->
                    _uiState.value = CounselListUiState.Error(err.message ?: "상담 삭제에 실패했습니다.")
                    sendError(err.message ?: "상담 삭제 실패")
                }
        }
    }
}
