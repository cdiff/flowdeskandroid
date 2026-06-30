package com.example.flowdesk_android.feature.mypage.presentation.main

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import com.example.flowdesk_android.feature.auth.domain.usecase.AuthenticateSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed class MyPageUiState {
    object Loading : MyPageUiState()
    data class Success(val user: AuthMeInfo) : MyPageUiState()
    data class Error(val message: String) : MyPageUiState()
}

sealed class MyPageEvent {
    object NavigateToLogin : MyPageEvent()
    data class ShowToast(val message: String) : MyPageEvent()
}

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authSessionUseCase: AuthenticateSessionUseCase
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. [핵심] 선언형 UI 상태 파이프라인
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MyPageUiState> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(MyPageUiState.Loading)
                authRepository.getMe()
                    .onSuccess { emit(MyPageUiState.Success(it)) }
                    .onFailure { emit(MyPageUiState.Error(it.message ?: "")) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyPageUiState.Loading)

    private val _event = Channel<MyPageEvent>()
    val event: Flow<MyPageEvent> = _event.receiveAsFlow()

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun logout() {
        viewModelScope.launch {
            authSessionUseCase.logout()
            _event.send(MyPageEvent.NavigateToLogin)
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            authRepository.logoutAll()
            _event.send(MyPageEvent.NavigateToLogin)
        }
    }
}

