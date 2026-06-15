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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(val reservations: Map<LocalDate, List<CounselItem>>) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}

@HiltViewModel
class CounselCalendarViewModel @Inject constructor(
    private val counselRepository: CounselRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow<YearMonth>(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedEmpSeq = MutableStateFlow<Int?>(null)
    val selectedEmpSeq: StateFlow<Int?> = _selectedEmpSeq.asStateFlow()

    private val _employeeList = MutableStateFlow<List<EmployeeStat>>(emptyList())
    val employeeList: StateFlow<List<EmployeeStat>> = _employeeList.asStateFlow()

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _monthlyReservationCount = MutableStateFlow(0)
    val monthlyReservationCount: StateFlow<Int> = _monthlyReservationCount.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow<String>("")
    val lastRefreshTime: StateFlow<String> = _lastRefreshTime.asStateFlow()

    init {
        // Load the manager list from dashboard statistics (which contains employeeStats)
        fetchManagers()

        // Automatically reload reservations when selectedMonth or selectedEmpSeq changes
        viewModelScope.launch {
            combine(selectedMonth, selectedEmpSeq) { month, empSeq ->
                Pair(month, empSeq)
            }.collect {
                loadReservations()
            }
        }
    }

    fun fetchManagers() {
        viewModelScope.launch {
            counselRepository.getDashboard(null, null)
                .onSuccess { dashboard ->
                    _employeeList.value = dashboard.employeeStats
                }
                .onFailure {
                    // Silently fail or keep empty
                }
        }
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
        fetchManagers()
        loadReservations()
    }

    private fun loadReservations() {
        val month = _selectedMonth.value
        val empSeq = _selectedEmpSeq.value

        // Calculate grid range: include Sunday overflow of 1st week and Saturday overflow of last week
        val firstDay = month.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 0=Sunday, 1=Monday ... 6=Saturday
        val gridStartDate = firstDay.minusDays(firstDayOfWeek.toLong())

        val lastDay = month.atEndOfMonth()
        val lastDayOfWeek = lastDay.dayOfWeek.value % 7
        val gridEndDate = lastDay.plusDays((6 - lastDayOfWeek).toLong())

        _uiState.value = CalendarUiState.Loading

        viewModelScope.launch {
            counselRepository.getCounsels(
                limit = 1000,
                resvStartDate = gridStartDate.toString(),
                resvEndDate = gridEndDate.toString(),
                empSeq = empSeq
            ).onSuccess { counselList ->
                // Map reservations by LocalDate
                val mapped = counselList.items
                    .filter { !it.counselResvDtm.isNullOrBlank() }
                    .groupBy { parseLocalDate(it.counselResvDtm)!! }

                // Count reservations purely inside the selected month
                val thisMonthCount = counselList.items.count { item ->
                    val date = parseLocalDate(item.counselResvDtm)
                    date != null && date.year == month.year && date.monthValue == month.monthValue
                }

                _monthlyReservationCount.value = thisMonthCount
                _uiState.value = CalendarUiState.Success(mapped)

                // Update refresh timestamp
                val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                _lastRefreshTime.value = java.time.LocalTime.now().format(formatter) + " 기준"
            }.onFailure { exception ->
                _uiState.value = CalendarUiState.Error(exception.message ?: "데이터 조회 오류")
            }
        }
    }

    private fun parseLocalDate(dtm: String?): LocalDate? {
        if (dtm.isNullOrBlank()) return null
        return try {
            val dateStr = if (dtm.contains("T")) dtm.substringBefore("T") else dtm.substringBefore(" ")
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
