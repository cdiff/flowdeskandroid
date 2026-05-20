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

    private val _uiState = MutableStateFlow<MyPageUiState>(MyPageUiState.Loading)
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private val _event = Channel<MyPageEvent>()
    val event: Flow<MyPageEvent> = _event.receiveAsFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = MyPageUiState.Loading
            authRepository.getMe()
                .onSuccess { _uiState.value = MyPageUiState.Success(it) }
                .onFailure { _uiState.value = MyPageUiState.Error(it.message ?: "조회 실패") }
        }
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

