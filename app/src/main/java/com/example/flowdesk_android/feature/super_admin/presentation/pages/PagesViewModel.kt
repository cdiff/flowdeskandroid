package com.example.flowdesk_android.feature.super_admin.presentation.pages

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Page
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

// ── UI State ─────────────────────────────────────────────
sealed class PageListUiState {
    object Loading : PageListUiState()
    object Empty : PageListUiState()
    data class Success(val pages: List<Page>) : PageListUiState()
    data class Error(val message: String) : PageListUiState()
}

// ── One-shot Events ───────────────────────────────────────
sealed class PageListEvent {
    object PageCreated : PageListEvent()
    object PageUpdated : PageListEvent()
    object PageDeleted : PageListEvent()
    data class Error(val message: String) : PageListEvent()
}

@HiltViewModel
class PagesViewModel @Inject constructor(
    private val superRepository: SuperRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PageListUiState>(PageListUiState.Loading)
    val uiState: StateFlow<PageListUiState> = _uiState.asStateFlow()

    private val _allPages = MutableStateFlow<List<Page>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    
    private val _expandedParents = MutableStateFlow<Set<Int>>(emptySet())
    val expandedParents: StateFlow<Set<Int>> = _expandedParents.asStateFlow()

    val filteredPages: StateFlow<List<Page>> = combine(_allPages, _searchQuery, _expandedParents) { pages, query, expanded ->
        if (query.isNotBlank()) {
            pages.filter {
                it.pageName.contains(query, ignoreCase = true) ||
                it.displayName.contains(query, ignoreCase = true) ||
                it.path.contains(query, ignoreCase = true)
            }
        } else {
            val result = mutableListOf<Page>()
            val roots = pages.filter { it.parentId == null }.sortedBy { it.sortOrder }
            val childrenMap = pages.filter { it.parentId != null }.groupBy { it.parentId }

            for (root in roots) {
                result.add(root)
                if (expanded.contains(root.pageId)) {
                    childrenMap[root.pageId]?.sortedBy { it.sortOrder }?.let { children ->
                        result.addAll(children)
                    }
                }
            }
            result
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _event = Channel<PageListEvent>()
    val event: Flow<PageListEvent> = _event.receiveAsFlow()

    init { fetchPages() }

    fun fetchPages() {
        viewModelScope.launch {
            _uiState.value = PageListUiState.Loading
            superRepository.getPages()
                .onSuccess { pages ->
                    _allPages.value = pages
                    _uiState.value = if (pages.isEmpty()) PageListUiState.Empty
                                     else PageListUiState.Success(pages)
                }
                .onFailure { _uiState.value = PageListUiState.Error(it.message ?: "조회 실패") }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun toggleParent(pageId: Int) {
        val current = _expandedParents.value.toMutableSet()
        if (current.contains(pageId)) {
            current.remove(pageId)
        } else {
            current.add(pageId)
        }
        _expandedParents.value = current
    }

    fun createPage(
        pageName: String,
        path: String,
        displayName: String,
        description: String?,
        parentId: Int?,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            superRepository.createPage(pageName, path, displayName, description, parentId, sortOrder)
                .onSuccess {
                    _event.send(PageListEvent.PageCreated)
                    fetchPages()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "생성 실패")) }
        }
    }

    fun updatePageStatus(page: Page, isActive: Boolean) {
        viewModelScope.launch {
            superRepository.updatePage(
                pageId = page.pageId,
                pageName = null,
                path = null,
                displayName = null,
                description = null,
                parentId = null,
                sortOrder = null,
                isActive = if (isActive) 1 else 0
            )
                .onSuccess {
                    _event.send(PageListEvent.PageUpdated)
                    fetchPages()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun deletePage(pageId: Int) {
        viewModelScope.launch {
            superRepository.deletePage(pageId)
                .onSuccess {
                    _event.send(PageListEvent.PageDeleted)
                    fetchPages()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "삭제 실패")) }
        }
    }
}
