package com.example.flowdesk_android.feature.counsel_management.domain.repository

import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselStatusUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDetail
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselList
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselMemo
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselLog

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

    suspend fun getCounselDetail(id: Int): Result<CounselDetail>

    suspend fun updateCounsel(id: Int, request: CounselUpdateRequest): Result<CounselDetail>

    suspend fun updateCounselStatus(id: Int, request: CounselStatusUpdateRequest): Result<Unit>

    suspend fun addCounselMemo(id: Int, memoText: String): Result<CounselMemo>

    suspend fun getCounselMemos(id: Int): Result<List<CounselMemo>>

    suspend fun getCounselLogs(id: Int): Result<List<CounselLog>>

    suspend fun deleteCounsel(id: Int): Result<Unit>
}
