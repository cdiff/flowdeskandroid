package com.example.flowdesk_android.presentation.ui.super_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.SuperDashboardResponse
import com.example.flowdesk_android.domain.usecase.GetSuperDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuperDashboardViewModel @Inject constructor(
    private val getSuperDashboardUseCase: GetSuperDashboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            getSuperDashboardUseCase()
                .onSuccess { data ->
                    _uiState.value = DashboardUiState.Success(data)
                }
                .onFailure { error ->
                    _uiState.value = DashboardUiState.Error(error.message ?: "알 수 없는 오류")
                }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: SuperDashboardResponse) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
