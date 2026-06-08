package com.example.flowdesk_android.feature.counsel_management.domain.repository

import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard

interface CounselRepository {
    suspend fun getDashboard(startDate: String? = null, endDate: String? = null): Result<CounselDashboard>
}
