package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.UserDto
import com.example.flowdesk_android.domain.usecase.GetMeUseCase
import com.example.flowdesk_android.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    data class LoadSuccess(val user: UserDto) : EditProfileState()
    data class UpdateSuccess(val user: UserDto) : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getMeUseCase: GetMeUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = EditProfileState.Loading
            getMeUseCase().onSuccess { response ->
                _state.value = EditProfileState.LoadSuccess(response.user)
            }.onFailure { e ->
                _state.value = EditProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(userName: String, userEmail: String, userTel: String?, userHp: String?) {
        viewModelScope.launch {
            _state.value = EditProfileState.Loading
            updateProfileUseCase(userName, userEmail, userTel, userHp)
                .onSuccess { user ->
                    _state.value = EditProfileState.UpdateSuccess(user)
                }
                .onFailure { e ->
                    _state.value = EditProfileState.Error(e.message ?: "Failed to update profile")
                }
        }
    }
}
