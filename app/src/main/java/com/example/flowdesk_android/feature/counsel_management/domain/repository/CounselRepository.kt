package com.example.flowdesk_android.feature.counsel_management.domain.repository

import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselList

interface CounselRepository {
    suspend fun getDashboard(startDate: String? = null, endDate: String? = null): Result<CounselDashboard>

    suspend fun getCounsels(
        page: Int? = null,
        limit: Int? = null,
        query: String? = null,
        counselStat: Int? = null,
        empSeq: Int? = null,
        webCode: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        duplicateState: String? = null,
        resvStartDate: String? = null,
        resvEndDate: String? = null
    ): Result<CounselList>
}
