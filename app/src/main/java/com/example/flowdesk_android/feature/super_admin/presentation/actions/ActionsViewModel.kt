package com.example.flowdesk_android.feature.super_admin.presentation.actions

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val _uiState = MutableStateFlow<ActionListUiState>(ActionListUiState.Loading)
    val uiState: StateFlow<ActionListUiState> = _uiState.asStateFlow()

    private val _allActions = MutableStateFlow<List<Action>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val filteredActions: StateFlow<List<Action>> = combine(_allActions, _searchQuery) { actions, query ->
        if (query.isBlank()) {
            actions
        } else {
            actions.filter {
                it.actionName.contains(query, ignoreCase = true) ||
                it.displayName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _event = Channel<ActionListEvent>()
    val event: Flow<ActionListEvent> = _event.receiveAsFlow()

    init { fetchActions() }

    fun fetchActions() {
        viewModelScope.launch {
            _uiState.value = ActionListUiState.Loading
            superRepository.getActions()
                .onSuccess { actions ->
                    _allActions.value = actions
                    _uiState.value = if (actions.isEmpty()) ActionListUiState.Empty
                                     else ActionListUiState.Success(actions)
                }
                .onFailure { _uiState.value = ActionListUiState.Error(it.message ?: "조회 실패") }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun createAction(actionName: String, displayName: String) {
        viewModelScope.launch {
            superRepository.createAction(actionName, displayName)
                .onSuccess {
                    _event.send(ActionListEvent.ActionCreated)
                    fetchActions()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "생성 실패")) }
        }
    }

    fun toggleStatus(action: Action) {
        viewModelScope.launch {
            superRepository.updateActionStatus(action.actionId, !action.isActive)
                .onSuccess {
                    _event.send(ActionListEvent.ActionStatusChanged)
                    fetchActions()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun deleteAction(actionId: Int) {
        viewModelScope.launch {
            superRepository.deleteAction(actionId)
                .onSuccess {
                    _event.send(ActionListEvent.ActionDeleted)
                    fetchActions()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "삭제 실패")) }
        }
    }
}
