package com.example.flowdesk_android.feature.auth.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import com.example.flowdesk_android.feature.auth.domain.usecase.AuthenticateSessionUseCase
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
    data class Success(val data: AuthMeInfo) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

sealed class DashboardEffect {
    object NavigateToLogin : DashboardEffect()
    data class ShowToast(val message: String) : DashboardEffect()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authSessionUseCase: AuthenticateSessionUseCase
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
            authRepository.getMe().onSuccess { response ->
                _dashboardState.value = DashboardState.Success(response)
            }.onFailure { exception ->
                _dashboardState.value = DashboardState.Error(exception.message ?: "Unknown error")
                _dashboardEffect.emit(DashboardEffect.ShowToast("Info Fetch Failed: ${exception.message}"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authSessionUseCase.logout()
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
            authRepository.logoutAll()
                .onSuccess {
                    _dashboardEffect.emit(DashboardEffect.NavigateToLogin)
                }
                .onFailure {
                     _dashboardEffect.emit(DashboardEffect.NavigateToLogin)
                }
        }
    }
}

