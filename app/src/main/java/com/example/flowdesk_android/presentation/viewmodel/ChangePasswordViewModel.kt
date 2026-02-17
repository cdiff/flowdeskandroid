package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.domain.usecase.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    fun changePassword(current: String, new: String, confirm: String) {
        viewModelScope.launch {
            _state.value = ChangePasswordState.Loading
            changePasswordUseCase(current, new, confirm)
                .onSuccess {
                    _state.value = ChangePasswordState.Success
                }
                .onFailure { e ->
                    _state.value = ChangePasswordState.Error(e.message ?: "Failed to change password")
                }
        }
    }
    
    fun resetState() {
        _state.value = ChangePasswordState.Idle
    }
}
