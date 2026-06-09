package com.example.flowdesk_android.feature.counsel_management.data.repository

import com.example.flowdesk_android.feature.counsel_management.data.api.CounselApi
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselList
import com.example.flowdesk_android.feature.counsel_management.domain.repository.CounselRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounselRepositoryImpl @Inject constructor(
    private val api: CounselApi
) : CounselRepository {

    override suspend fun getDashboard(startDate: String?, endDate: String?): Result<CounselDashboard> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getDashboard(startDate, endDate)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상담 대시보드 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun getCounsels(
        page: Int?,
        limit: Int?,
        query: String?,
        counselStat: Int?,
        empSeq: Int?,
        webCode: String?,
        startDate: String?,
        endDate: String?,
        duplicateState: String?,
        resvStartDate: String?,
        resvEndDate: String?
    ): Result<CounselList> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getCounsels(
                page, limit, query, counselStat, empSeq, webCode,
                startDate, endDate, duplicateState, resvStartDate, resvEndDate
            )
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상담 목록 조회 실패 (${response.code()})")
            }
        }
    }
}
