package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class DeleteActionUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(actionId: Int): Result<Unit> =
        repository.deleteAction(actionId)
}
