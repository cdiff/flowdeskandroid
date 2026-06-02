package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Page
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class GetPagesUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null
    ): Result<List<Page>> = repository.getPages(page, limit, search)
}
