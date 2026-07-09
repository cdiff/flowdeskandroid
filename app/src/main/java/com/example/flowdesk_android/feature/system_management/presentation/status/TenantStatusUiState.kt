package com.example.flowdesk_android.feature.system_management.presentation.status

import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusGroup

data class TenantStatusUiState(
    val isLoading: Boolean = false,
    val selectedGroup: String = "all",
    val statusGroups: List<String> = emptyList(),
    val filteredGroups: List<TenantStatusGroup> = emptyList(),
    val totalGroups: Int = 0,
    val totalStatuses: Int = 0,
    val activeStatuses: Int = 0,
    val inactiveStatuses: Int = 0,
    val canWrite: Boolean = false,
    val canUpdate: Boolean = false,
    val canDelete: Boolean = false
)
