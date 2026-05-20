package com.example.flowdesk_android.feature.mypage.presentation.edit_profile

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
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

sealed class EditProfileUiState {
    object Loading : EditProfileUiState()
    data class Success(val user: AuthMeInfo) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

sealed class EditProfileEvent {
    object Updated : EditProfileEvent()
    data class Error(val message: String) : EditProfileEvent()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _event = Channel<EditProfileEvent>()
    val event: Flow<EditProfileEvent> = _event.receiveAsFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            authRepository.getMe()
                .onSuccess { _uiState.value = EditProfileUiState.Success(it) }
                .onFailure { _uiState.value = EditProfileUiState.Error(it.message ?: "조회 실패") }
        }
    }

    fun updateProfile(userName: String, userEmail: String, corpName: String?, userTel: String?, userHp: String?) {
        viewModelScope.launch {
            authRepository.updateProfile(userName, userEmail, userTel, userHp)
                .onSuccess {
                    _event.send(EditProfileEvent.Updated)
                    loadProfile()
                }
                .onFailure { _event.send(EditProfileEvent.Error(it.message ?: "수정 실패")) }
        }
    }
}

