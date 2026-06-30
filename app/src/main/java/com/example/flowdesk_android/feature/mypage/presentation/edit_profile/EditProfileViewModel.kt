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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. [핵심] 선언형 UI 상태 파이프라인
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EditProfileUiState> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(EditProfileUiState.Loading)
                authRepository.getMe()
                    .onSuccess { emit(EditProfileUiState.Success(it)) }
                    .onFailure { emit(EditProfileUiState.Error(it.message ?: "")) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditProfileUiState.Loading)

    private val _event = Channel<EditProfileEvent>()
    val event: Flow<EditProfileEvent> = _event.receiveAsFlow()

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun updateProfile(userName: String, userEmail: String, corpName: String?, userTel: String?, userHp: String?) {
        viewModelScope.launch {
            authRepository.updateProfile(userName, userEmail, userTel, userHp)
                .onSuccess {
                    _event.send(EditProfileEvent.Updated)
                    triggerRefresh()
                }
                .onFailure { _event.send(EditProfileEvent.Error(it.message ?: "")) }
        }
    }
}

