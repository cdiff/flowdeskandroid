package com.example.flowdesk_android.feature.counsel_management.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselItem
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.example.flowdesk_android.feature.counsel_management.domain.repository.CounselRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

import com.example.flowdesk_android.feature.counsel_management.domain.usecase.GetCalendarReservationsUseCase

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(val reservations: Map<LocalDate, List<CounselItem>>) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}

@HiltViewModel
class CounselCalendarViewModel @Inject constructor(
    private val counselRepository: CounselRepository,
    private val getCalendarReservationsUseCase: GetCalendarReservationsUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow<YearMonth>(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedEmpSeq = MutableStateFlow<Int?>(null)
    val selectedEmpSeq: StateFlow<Int?> = _selectedEmpSeq.asStateFlow()

    private val _monthlyReservationCount = MutableStateFlow(0)
    val monthlyReservationCount: StateFlow<Int> = _monthlyReservationCount.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow<String>("")
    val lastRefreshTime: StateFlow<String> = _lastRefreshTime.asStateFlow()

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 관리자(상담 매니저) 목록 반응형 로드
    @OptIn(ExperimentalCoroutinesApi::class)
    val employeeList: StateFlow<List<EmployeeStat>> = _refreshTrigger
        .flatMapLatest {
            flow {
                counselRepository.getDashboard(null, null)
                    .onSuccess { emit(it.employeeStats) }
                    .onFailure { emit(emptyList<EmployeeStat>()) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. [핵심] 선언형 UI 상태 파이프라인
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = combine(
        selectedMonth,
        selectedEmpSeq,
        _refreshTrigger
    ) { month, empSeq, _ ->
        Triple(month, empSeq, _refreshTrigger.value)
    }.flatMapLatest { (month, empSeq, triggerVal) ->
        flow {
            emit(CalendarUiState.Loading)
            getCalendarReservationsUseCase(month, empSeq).fold(
                onSuccess = { data ->
                    _monthlyReservationCount.value = data.monthlyCount

                    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                    _lastRefreshTime.value = java.time.LocalTime.now().format(formatter) + " 기준"

                    emit(CalendarUiState.Success(data.reservations))
                },
                onFailure = { err ->
                    emit(CalendarUiState.Error(err.message ?: "데이터 조회 오류"))
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState.Loading)

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun selectPrevMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun selectNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun selectToday() {
        _selectedMonth.value = YearMonth.now()
    }

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun selectEmpSeq(empSeq: Int?) {
        _selectedEmpSeq.value = empSeq
    }

    fun refreshReservations() {
        triggerRefresh()
    }
}
