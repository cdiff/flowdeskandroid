package com.example.flowdesk_android.feature.super_admin.presentation.pages

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Page
import com.example.flowdesk_android.feature.super_admin.domain.usecase.CreatePageUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.DeletePageUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.GetPagesUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.UpdatePageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val getPagesUseCase: GetPagesUseCase,
    private val createPageUseCase: CreatePageUseCase,
    private val updatePageUseCase: UpdatePageUseCase,
    private val deletePageUseCase: DeletePageUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PageListUiState>(PageListUiState.Loading)
    val uiState: StateFlow<PageListUiState> = _uiState.asStateFlow()

    private val _filteredPages = MutableStateFlow<List<Page>>(emptyList())
    val filteredPages: StateFlow<List<Page>> = _filteredPages.asStateFlow()

    private var allPages: List<Page> = emptyList()
    val expandedParents = mutableSetOf<Int>()
    private var currentSearchQuery = ""

    private val _event = Channel<PageListEvent>()
    val event: Flow<PageListEvent> = _event.receiveAsFlow()

    init { fetchPages() }

    fun fetchPages() {
        viewModelScope.launch {
            _uiState.value = PageListUiState.Loading
            getPagesUseCase()
                .onSuccess { pages ->
                    allPages = pages
                    updateFilteredPages()
                    _uiState.value = if (pages.isEmpty()) PageListUiState.Empty
                                     else PageListUiState.Success(pages)
                }
                .onFailure { _uiState.value = PageListUiState.Error(it.message ?: "오류 발생") }
        }
    }

    fun search(query: String) {
        currentSearchQuery = query
        updateFilteredPages()
    }

    fun toggleParent(pageId: Int) {
        if (expandedParents.contains(pageId)) expandedParents.remove(pageId)
        else expandedParents.add(pageId)
        updateFilteredPages()
    }

    private fun updateFilteredPages() {
        if (currentSearchQuery.isNotBlank()) {
            // 검색 시에는 평면 리스트로 일치하는 항목만 표시
            _filteredPages.value = allPages.filter {
                it.pageName.contains(currentSearchQuery, ignoreCase = true) ||
                it.displayName.contains(currentSearchQuery, ignoreCase = true) ||
                it.path.contains(currentSearchQuery, ignoreCase = true)
            }
        } else {
            // 기본 계층 뷰: 부모 페이지 + 펼쳐진 부모의 자식 페이지
            val result = mutableListOf<Page>()
            val roots = allPages.filter { it.parentId == null }.sortedBy { it.sortOrder }
            val childrenMap = allPages.filter { it.parentId != null }.groupBy { it.parentId }

            for (root in roots) {
                result.add(root)
                if (expandedParents.contains(root.pageId)) {
                    childrenMap[root.pageId]?.sortedBy { it.sortOrder }?.let { children ->
                        result.addAll(children)
                    }
                }
            }
            _filteredPages.value = result
        }
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
            createPageUseCase(pageName, path, displayName, description, parentId, sortOrder)
                .onSuccess {
                    _event.send(PageListEvent.PageCreated)
                    fetchPages()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "페이지 생성 실패")) }
        }
    }

    fun updatePageStatus(page: Page, isActive: Boolean) {
        viewModelScope.launch {
            updatePageUseCase(pageId = page.pageId, isActive = if (isActive) 1 else 0)
                .onSuccess {
                    _event.send(PageListEvent.PageUpdated)
                    fetchPages()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun deletePage(pageId: Int) {
        viewModelScope.launch {
            deletePageUseCase(pageId)
                .onSuccess {
                    _event.send(PageListEvent.PageDeleted)
                    fetchPages()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "페이지 삭제 실패")) }
        }
    }
}
