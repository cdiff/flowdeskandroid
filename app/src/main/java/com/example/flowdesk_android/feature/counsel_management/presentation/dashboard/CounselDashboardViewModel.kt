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

    private val _uiState = MutableStateFlow<CounselDashboardUiState>(CounselDashboardUiState.Loading)
    val uiState: StateFlow<CounselDashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard(startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            _uiState.value = CounselDashboardUiState.Loading
            counselRepository.getDashboard(startDate, endDate)
                .onSuccess { _uiState.value = CounselDashboardUiState.Success(it) }
                .onFailure { _uiState.value = CounselDashboardUiState.Error(it.message ?: "오류 발생") }
        }
    }
}
