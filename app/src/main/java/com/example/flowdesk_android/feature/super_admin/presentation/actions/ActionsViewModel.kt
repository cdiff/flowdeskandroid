package com.example.flowdesk_android.feature.super_admin.presentation.actions

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.example.flowdesk_android.feature.super_admin.domain.usecase.CreateActionUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.DeleteActionUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.GetActionsUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.UpdateActionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val getActionsUseCase: GetActionsUseCase,
    private val createActionUseCase: CreateActionUseCase,
    private val deleteActionUseCase: DeleteActionUseCase,
    private val updateActionStatusUseCase: UpdateActionStatusUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<ActionListUiState>(ActionListUiState.Loading)
    val uiState: StateFlow<ActionListUiState> = _uiState.asStateFlow()

    private val _filteredActions = MutableStateFlow<List<Action>>(emptyList())
    val filteredActions: StateFlow<List<Action>> = _filteredActions.asStateFlow()

    private var allActions: List<Action> = emptyList()
    private var currentSearchQuery = ""

    private val _event = Channel<ActionListEvent>()
    val event: Flow<ActionListEvent> = _event.receiveAsFlow()

    init { fetchActions() }

    fun fetchActions() {
        viewModelScope.launch {
            _uiState.value = ActionListUiState.Loading
            getActionsUseCase()
                .onSuccess { actions ->
                    allActions = actions
                    applyFilter()
                    _uiState.value = if (actions.isEmpty()) ActionListUiState.Empty
                                     else ActionListUiState.Success(actions)
                }
                .onFailure { _uiState.value = ActionListUiState.Error(it.message ?: "오류 발생") }
        }
    }

    fun search(query: String) {
        currentSearchQuery = query
        applyFilter()
    }

    private fun applyFilter() {
        _filteredActions.value = if (currentSearchQuery.isBlank()) {
            allActions
        } else {
            allActions.filter {
                it.actionName.contains(currentSearchQuery, ignoreCase = true) ||
                it.displayName.contains(currentSearchQuery, ignoreCase = true)
            }
        }
    }

    fun createAction(actionName: String, displayName: String) {
        viewModelScope.launch {
            createActionUseCase(actionName, displayName)
                .onSuccess {
                    _event.send(ActionListEvent.ActionCreated)
                    fetchActions()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "액션 생성 실패")) }
        }
    }

    fun toggleStatus(action: Action) {
        viewModelScope.launch {
            updateActionStatusUseCase(action.actionId, !action.isActive)
                .onSuccess {
                    _event.send(ActionListEvent.ActionStatusChanged)
                    fetchActions()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun deleteAction(actionId: Int) {
        viewModelScope.launch {
            deleteActionUseCase(actionId)
                .onSuccess {
                    _event.send(ActionListEvent.ActionDeleted)
                    fetchActions()
                }
                .onFailure { _event.send(ActionListEvent.Error(it.message ?: "액션 삭제 실패")) }
        }
    }
}
