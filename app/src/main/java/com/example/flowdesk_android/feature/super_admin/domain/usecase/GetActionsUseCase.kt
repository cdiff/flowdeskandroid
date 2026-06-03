package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class GetActionsUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null
    ): Result<List<Action>> = repository.getActions(page, limit, search)
}
