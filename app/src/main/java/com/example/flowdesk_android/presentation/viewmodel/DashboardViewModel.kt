package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.AuthMeResponse
import com.example.flowdesk_android.domain.usecase.GetMeUseCase
import com.example.flowdesk_android.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val data: AuthMeResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

sealed class DashboardEffect {
    object NavigateToLogin : DashboardEffect()
    data class ShowToast(val message: String) : DashboardEffect()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getMeUseCase: GetMeUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _dashboardEffect = MutableSharedFlow<DashboardEffect>()
    val dashboardEffect: SharedFlow<DashboardEffect> = _dashboardEffect.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            getMeUseCase().onSuccess { response ->
                _dashboardState.value = DashboardState.Success(response)
            }.onFailure { exception ->
                _dashboardState.value = DashboardState.Error(exception.message ?: "Unknown error")
                _dashboardEffect.emit(DashboardEffect.ShowToast("Info Fetch Failed: ${exception.message}"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase(allDevices = false)
                .onSuccess {
                    _dashboardEffect.emit(DashboardEffect.NavigateToLogin)
                }
                .onFailure {
                    _dashboardEffect.emit(DashboardEffect.NavigateToLogin)
                }
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            logoutUseCase(allDevices = true)
                .onSuccess {
                    _dashboardEffect.emit(DashboardEffect.NavigateToLogin)
                }
                .onFailure {
                     _dashboardEffect.emit(DashboardEffect.NavigateToLogin)
                }
        }
    }
}
