package com.example.flowdesk_android.feature.super_admin.presentation.actions

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────
sealed class ActionListUiState {
    object Loading : ActionListUiState()
    object Empty : ActionListUiState()
    data class Success(val actions: List<Action>) : ActionListUiState()
    data class Error(val message: String) : ActionListUiState()
}

// ── One-shot Events ───────────────────────────────────────────
sealed class ActionListEvent {
    object ActionCreated : ActionListEvent()
    object ActionDeleted : ActionListEvent()
    object ActionStatusChanged : ActionListEvent()
    data class Error(val message: String) : ActionListEvent()
}

@HiltViewModel
class ActionsViewModel @Inject constructor(
    private val superRepository: SuperRepository
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 전체 액션 가져오는 흐름
    @OptIn(ExperimentalCoroutinesApi::class)
    private val actionsFlow: Flow<Result<List<Action>>> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(Result.success(emptyList())) // 로딩 상태 전이를 위해 발행
                val res = superRepository.getActions()
                emit(res)
            }
        }

    // 3. UI 로딩/에러/성공 상태 uiState
    val uiState: StateFlow<ActionListUiState> = actionsFlow.map { result ->
        if (_refreshTrigger.value > 0 && result.getOrNull() == null) {
            ActionListUiState.Error(result.exceptionOrNull()?.message ?: "조회 실패")
        } else {
            result.fold(
                onSuccess = { actions ->
                    if (actions.isEmpty() && _refreshTrigger.value == 0) ActionListUiState.Loading
                    else if (actions.isEmpty()) ActionListUiState.Empty
                    else ActionListUiState.Success(actions)
                },
                onFailure = { e ->
                    ActionListUiState.Error(e.message ?: "조회 실패")
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActionListUiState.Loading)

    // 4. 전체 액션 캐시 StateFlow
    private val allActions: StateFlow<List<Action>> = actionsFlow.map { result ->
        result.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 5. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 6. 실시간 필터링된 액션 목록
    val filteredActions: StateFlow<List<Action>> = combine(allActions, debouncedQuery) { actions, query ->
        if (query.isBlank()) {
            actions
        } else {
            actions.filter {
                it.actionName.contains(query, ignoreCase = true) ||
                it.displayName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _event = Channel<ActionListEvent>()
    val event: Flow<ActionListEvent> = _event.receiveAsFlow()

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun createAction(actionName: String, displayName: String) {
        viewModelScope.launch {
            superRepository.createAction(actionName, displayName)
                .onSuccess {
                    _event.send(ActionListEvent.ActionCreated)
                    triggerRefresh()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "생성 실패")) }
        }
    }

    fun toggleStatus(action: Action) {
        viewModelScope.launch {
            superRepository.updateActionStatus(action.actionId, !action.isActive)
                .onSuccess {
                    _event.send(ActionListEvent.ActionStatusChanged)
                    triggerRefresh()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun deleteAction(actionId: Int) {
        viewModelScope.launch {
            superRepository.deleteAction(actionId)
                .onSuccess {
                    _event.send(ActionListEvent.ActionDeleted)
                    triggerRefresh()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "삭제 실패")) }
        }
    }
}
