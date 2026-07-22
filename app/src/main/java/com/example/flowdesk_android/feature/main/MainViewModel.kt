package com.example.flowdesk_android.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Success(val data: AuthMeInfo) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: com.example.flowdesk_android.data.local.SessionManager
) : ViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 캐시 세션 정보 StateFlow
    private val _cachedSession = MutableStateFlow<AuthMeInfo?>(null)

    // 3. [핵심] 선언형 UI 상태 파이프라인 (SessionManager 상태 변경 시 하단 네비 탭도 실시간 즉시 갱신)
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MainUiState> = combine(_refreshTrigger, sessionManager.sessionState) { trigger, session ->
        trigger to session
    }.flatMapLatest { (trigger, session) ->
        flow {
            if (session != null) {
                emit(MainUiState.Success(session))
            } else if (trigger > 0) {
                emit(MainUiState.Loading)
                authRepository.getMe()
                    .onSuccess { 
                        sessionManager.setSession(it)
                        emit(MainUiState.Success(it)) 
                    }
                    .onFailure { emit(MainUiState.Error(it.message ?: "인증 세션을 불러오지 못했습니다.")) }
            } else {
                emit(MainUiState.Idle)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState.Idle)

    fun init(authMeInfoJson: String?) {
        if (!authMeInfoJson.isNullOrEmpty()) {
            try {
                val info = com.google.gson.Gson().fromJson(authMeInfoJson, AuthMeInfo::class.java)
                _cachedSession.value = info
                sessionManager.setSession(info)
                return
            } catch (e: Exception) {
                // JSON 파싱 실패 시 서버 API 호출을 시도하기 위해 폴백 처리
            }
        }
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }
}
