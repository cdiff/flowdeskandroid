package com.example.flowdesk_android.feature.mypage.presentation.change_password

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun changePassword(current: String, new: String, confirm: String) {
        if (new != confirm) {
            _uiState.value = ChangePasswordUiState.Error("")
            return
        }
        viewModelScope.launch {
            _uiState.value = ChangePasswordUiState.Loading
            authRepository.changePassword(current, new, confirm)
                .onSuccess {
                    _uiState.value = ChangePasswordUiState.Success
                }
                .onFailure { _uiState.value = ChangePasswordUiState.Error(it.message ?: "") }
        }
    }

    fun resetState() { _uiState.value = ChangePasswordUiState.Idle }
}
