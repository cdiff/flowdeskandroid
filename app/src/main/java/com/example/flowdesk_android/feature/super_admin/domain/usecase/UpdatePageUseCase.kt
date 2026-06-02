package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Page
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class UpdatePageUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        pageId: Int,
        pageName: String? = null,
        path: String? = null,
        displayName: String? = null,
        description: String? = null,
        parentId: Int? = null,
        sortOrder: Int? = null,
        isActive: Int? = null
    ): Result<Page> = repository.updatePage(pageId, pageName, path, displayName, description, parentId, sortOrder, isActive)
}
