package com.example.flowdesk_android.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Success(val data: AuthMeInfo) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun init(authMeInfoJson: String?) {
        if (!authMeInfoJson.isNullOrEmpty()) {
            try {
                val info = com.google.gson.Gson().fromJson(authMeInfoJson, AuthMeInfo::class.java)
                _uiState.value = MainUiState.Success(info)
                return
            } catch (e: Exception) {
                // JSON 파싱 실패 시 서버 API 호출을 시도하기 위해 폴백 처리
            }
        }
        loadSession()
    }

    fun loadSession() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            authRepository.getMe()
                .onSuccess { info ->
                    _uiState.value = MainUiState.Success(info)
                }
                .onFailure { error ->
                    _uiState.value = MainUiState.Error(error.message ?: "인증 세션을 불러오지 못했습니다.")
                }
        }
    }
}
