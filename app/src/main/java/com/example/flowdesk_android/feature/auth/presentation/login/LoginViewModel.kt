package com.example.flowdesk_android.feature.auth.presentation.login

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.auth.domain.model.*
import com.example.flowdesk_android.feature.auth.domain.usecase.AuthenticateSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val info: com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authSessionUseCase: AuthenticateSessionUseCase,
    private val authRepository: com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(tenantName: String, userId: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val command = LoginCommand(AuthProvider.CREDENTIALS, tenantName, userId, password)
            authSessionUseCase.login(command)
                .onSuccess {
                    val session = authSessionUseCase.sessionState.value
                    if (session is AuthSession.Active) {
                        // 로그인 성공 후 메뉴 권한 정보를 선조회
                        authRepository.getMe()
                            .onSuccess { info ->
                                _uiState.value = LoginUiState.Success(info)
                            }
                            .onFailure { exception ->
                                _uiState.value = LoginUiState.Error(exception.message ?: "권한 정보를 가져오는데 실패했습니다.")
                            }
                    } else {
                        _uiState.value = LoginUiState.Error("세션 정보가 활성화되지 않았습니다.")
                    }
                }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "로그인 실패") }
        }
    }

    fun resetState() { _uiState.value = LoginUiState.Idle }
}

