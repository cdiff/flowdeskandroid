package com.example.flowdesk_android.feature.user_management.presentation.users.list

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.user_management.domain.model.User
import com.example.flowdesk_android.feature.user_management.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed class UserListUiState {
    object Loading : UserListUiState()
    object Empty : UserListUiState()
    data class Success(val users: List<User>) : UserListUiState()
    data class Error(val message: String) : UserListUiState()
}

sealed class UserListEvent {
    object TokensInvalidated : UserListEvent()
    object StatusToggled : UserListEvent()
}

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    // 1. 상태 보관용 MutableStateFlow 정의
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    private val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // 2. UI 로딩/에러/성공 상태 uiState 정의 (리액티브 결합)
    val uiState: StateFlow<UserListUiState> = combine(
        _allUsers,
        _isLoading,
        _errorMessage
    ) { users, loading, error ->
        if (error != null) {
            UserListUiState.Error(error)
        } else if (loading && users.isEmpty()) {
            UserListUiState.Loading
        } else if (users.isEmpty()) {
            UserListUiState.Empty
        } else {
            UserListUiState.Success(users)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserListUiState.Loading)

    // 3. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 4. 실시간 필터링된 유저 목록
    val filteredUsers: StateFlow<List<User>> = combine(allUsers, debouncedQuery) { users, query ->
        if (query.isBlank()) {
            users
        } else {
            val q = query.lowercase()
            users.filter {
                it.userName.lowercase().contains(q) ||
                it.userId.lowercase().contains(q) ||
                (it.userEmail?.lowercase()?.contains(q) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _event = Channel<UserListEvent>()
    val event: Flow<UserListEvent> = _event.receiveAsFlow()

    init {
        // 초기 로딩 트리거
        triggerRefresh()
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    // 5. 서버로부터 전체 데이터 가져오기 (초기 진입/새로고침 시에만 사용)
    fun triggerRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            userRepository.getUsers()
                .onSuccess { users ->
                    _allUsers.value = users
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "알 수 없는 오류"
                }
            _isLoading.value = false
        }
    }

    fun toggleUserStatus(userId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentStatus
            userRepository.updateUserStatus(userId, newStatus)
                .onSuccess {
                    // 🚀 로컬 상태 직접 변경 (상태값 토글 반영)
                    _allUsers.value = _allUsers.value.map { user ->
                        if (user.userSeq == userId) {
                            user.copy(isActive = newStatus)
                        } else {
                            user
                        }
                    }
                    _event.send(UserListEvent.StatusToggled)
                }
                .onFailure { e ->
                    sendError(e.message ?: "상태 변경 실패")
                }
        }
    }

    fun invalidateTokens(userId: Int) {
        viewModelScope.launch {
            userRepository.invalidateUserTokens(userId)
                .onSuccess {
                    triggerRefresh() // 토큰 무효화는 보안/세션 갱신이므로 전체 목록 갱신 수행
                    _event.send(UserListEvent.TokensInvalidated)
                }
                .onFailure { e ->
                    sendError(e.message ?: "로그아웃 실패")
                }
        }
    }
}
