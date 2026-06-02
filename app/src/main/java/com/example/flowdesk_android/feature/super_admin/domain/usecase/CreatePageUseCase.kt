package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Page
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class CreatePageUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        pageName: String,
        path: String,
        displayName: String,
        description: String? = null,
        parentId: Int? = null,
        sortOrder: Int = 1
    ): Result<Page> = repository.createPage(pageName, path, displayName, description, parentId, sortOrder)
}
