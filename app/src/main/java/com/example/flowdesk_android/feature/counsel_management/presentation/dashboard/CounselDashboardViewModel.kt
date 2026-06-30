package com.example.flowdesk_android.feature.counsel_management.presentation.dashboard

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard
import com.example.flowdesk_android.feature.counsel_management.domain.repository.CounselRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi

// ── UI State ──────────────────────────────────────────────
sealed class CounselDashboardUiState {
    object Loading : CounselDashboardUiState()
    data class Success(val data: CounselDashboard) : CounselDashboardUiState()
    data class Error(val message: String) : CounselDashboardUiState()
}

@HiltViewModel
class CounselDashboardViewModel @Inject constructor(
    private val counselRepository: CounselRepository
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 검색 날짜 기간 필터
    private val _dateRange = MutableStateFlow<Pair<String?, String?>>(null to null)

    // 3. [핵심] 선언형 UI 상태 파이프라인
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CounselDashboardUiState> = combine(
        _dateRange,
        _refreshTrigger
    ) { range, _ ->
        range
    }.flatMapLatest { (startDate, endDate) ->
        flow {
            emit(CounselDashboardUiState.Loading)
            counselRepository.getDashboard(startDate, endDate)
                .onSuccess { emit(CounselDashboardUiState.Success(it)) }
                .onFailure { emit(CounselDashboardUiState.Error(it.message ?: "오류 발생")) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CounselDashboardUiState.Loading)

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun loadDashboard(startDate: String? = null, endDate: String? = null) {
        _dateRange.value = startDate to endDate
    }
}
