package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class DeletePageUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(pageId: Int): Result<Unit> =
        repository.deletePage(pageId)
}
