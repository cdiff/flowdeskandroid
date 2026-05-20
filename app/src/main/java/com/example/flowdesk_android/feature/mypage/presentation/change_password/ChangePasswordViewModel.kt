package com.example.flowdesk_android.feature.mypage.presentation.change_password

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordUiState {
    object Idle : ChangePasswordUiState()
    object Loading : ChangePasswordUiState()
    object Success : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<ChangePasswordUiState>(ChangePasswordUiState.Idle)
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _event = Channel<String>()
    val successEvent: Flow<String> = _event.receiveAsFlow()

    fun changePassword(current: String, new: String, confirm: String) {
        if (new != confirm) {
            _uiState.value = ChangePasswordUiState.Error("새 비밀번호가 일치하지 않습니다.")
            return
        }
        viewModelScope.launch {
            _uiState.value = ChangePasswordUiState.Loading
            authRepository.changePassword(current, new, confirm)
                .onSuccess {
                    _uiState.value = ChangePasswordUiState.Success
                    _event.send("비밀번호가 변경되었습니다.")
                }
                .onFailure { _uiState.value = ChangePasswordUiState.Error(it.message ?: "변경 실패") }
        }
    }

    fun resetState() { _uiState.value = ChangePasswordUiState.Idle }
}
