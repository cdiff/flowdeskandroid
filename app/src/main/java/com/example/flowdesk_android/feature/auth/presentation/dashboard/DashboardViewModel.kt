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

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. [핵심] 선언형 UI 상태 파이프라인
    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardState: StateFlow<DashboardState> = _refreshTrigger
        .flatMapLatest { trigger ->
            flow {
                if (trigger == 0) {
                    emit(DashboardState.Idle)
                    return@flow
                }
                emit(DashboardState.Loading)
                authRepository.getMe().fold(
                    onSuccess = { emit(DashboardState.Success(it)) },
                    onFailure = { err ->
                        emit(DashboardState.Error(err.message ?: "Unknown error"))
                        _dashboardEffect.emit(DashboardEffect.ShowToast("Info Fetch Failed: ${err.message}"))
                    }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState.Idle)

    private val _dashboardEffect = MutableSharedFlow<DashboardEffect>()
    val dashboardEffect: SharedFlow<DashboardEffect> = _dashboardEffect.asSharedFlow()

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun loadDashboardData() {
        triggerRefresh()
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

