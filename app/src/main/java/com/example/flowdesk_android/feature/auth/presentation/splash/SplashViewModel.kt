package com.example.flowdesk_android.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.auth.domain.model.AuthSession
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import com.example.flowdesk_android.feature.auth.domain.usecase.AuthenticateSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashUiState {
    object Idle : SplashUiState()
    object Loading : SplashUiState()
    data class Success(val info: AuthMeInfo) : SplashUiState()
    object NavigateToLogin : SplashUiState()
    data class Error(val message: String) : SplashUiState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authSessionUseCase: AuthenticateSessionUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Idle)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading
            
            // 1. 로컬 저장 토큰을 기반으로 세션 초기화 실행
            authSessionUseCase.initializeSession()

            // 2. 초기화 완료된 세션 상태 가져오기
            val currentSession = authSessionUseCase.sessionState.value
            
            if (currentSession is AuthSession.Active) {
                // 3. 로그인 상태가 유지 중이면 권한 및 메뉴 정보 조회 API 호출
                fetchMenuInfo()
            } else {
                // 4. 게스트 또는 로그아웃 상태이면 로그인 화면으로 리다이렉트
                _uiState.value = SplashUiState.NavigateToLogin
            }
        }
    }

    private suspend fun fetchMenuInfo() {
        authRepository.getMe().onSuccess { info ->
            _uiState.value = SplashUiState.Success(info)
        }.onFailure { exception ->
            // 네트워크 오류 등으로 메뉴 정보 획득 실패 시, 에러 상태로 분기하거나 로그인 화면으로 넘김
            _uiState.value = SplashUiState.Error(exception.message ?: "메뉴 정보를 가져오는데 실패했습니다.")
        }
    }
}
