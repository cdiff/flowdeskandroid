package com.example.flowdesk_android.feature.super_admin.presentation.dashboard

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats
import com.example.flowdesk_android.feature.super_admin.domain.usecase.GetSuperDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SuperDashboardUiState {
    object Loading : SuperDashboardUiState()
    data class Success(val stats: DashboardStats) : SuperDashboardUiState()
    data class Error(val message: String) : SuperDashboardUiState()
}

@HiltViewModel
class SuperDashboardViewModel @Inject constructor(
    private val getSuperDashboardUseCase: GetSuperDashboardUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<SuperDashboardUiState>(SuperDashboardUiState.Loading)
    val uiState: StateFlow<SuperDashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = SuperDashboardUiState.Loading
            getSuperDashboardUseCase()
                .onSuccess { _uiState.value = SuperDashboardUiState.Success(it) }
                .onFailure { _uiState.value = SuperDashboardUiState.Error(it.message ?: "조회 실패") }
        }
    }
}
