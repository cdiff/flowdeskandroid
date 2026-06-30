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
}

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 서버에서 전체 유저를 가져오는 흐름
    @OptIn(ExperimentalCoroutinesApi::class)
    private val usersFlow: Flow<Result<List<User>>> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(Result.success(emptyList())) // 로딩 상태 전이를 위해 발행
                val res = userRepository.getUsers()
                emit(res)
            }
        }

    // 3. UI 로딩/에러/성공 상태를 방출하는 uiState
    val uiState: StateFlow<UserListUiState> = usersFlow.map { result ->
        if (_refreshTrigger.value > 0 && result.getOrNull() == null) {
            // 리프레시 시도 시 에러가 났을 때 처리
            UserListUiState.Error(result.exceptionOrNull()?.message ?: "알 수 없는 오류")
        } else {
            result.fold(
                onSuccess = { users ->
                    if (users.isEmpty() && _refreshTrigger.value == 0) UserListUiState.Loading // 최초 로딩
                    else if (users.isEmpty()) UserListUiState.Empty
                    else UserListUiState.Success(users)
                },
                onFailure = { e ->
                    UserListUiState.Error(e.message ?: "알 수 없는 오류")
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserListUiState.Loading)

    // 4. 전체 유저 목록 캐시 StateFlow
    private val allUsers: StateFlow<List<User>> = usersFlow.map { result ->
        result.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 5. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 6. 실시간 필터링된 유저 목록
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

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun toggleUserStatus(userId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            userRepository.updateUserStatus(userId, !currentStatus)
                .onSuccess {
                    triggerRefresh()
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
                    triggerRefresh()
                    _event.send(UserListEvent.TokensInvalidated)
                }
                .onFailure { e ->
                    sendError(e.message ?: "로그아웃 실패")
                }
        }
    }
}
