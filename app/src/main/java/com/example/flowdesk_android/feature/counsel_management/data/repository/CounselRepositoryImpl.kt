package com.example.flowdesk_android.feature.counsel_management.data.repository

import com.example.flowdesk_android.feature.counsel_management.data.api.CounselApi
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselStatusUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselMemoRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselMemoDto
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselLogDto
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDetail
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselList
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselMemo
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselLog
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

    override suspend fun getCounselDetail(id: Int): Result<CounselDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getCounselDetail(id)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상담 상세 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun updateCounsel(id: Int, request: CounselUpdateRequest): Result<CounselDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateCounsel(id, request)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상담 수정 실패 (${response.code()})")
            }
        }
    }

    override suspend fun updateCounselStatus(id: Int, request: CounselStatusUpdateRequest): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateCounselStatus(id, request)
            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string()?.take(200) ?: "없음"
                throw Exception("상태 변경 실패 (${response.code()}): $errorMsg")
            }
        }
    }

    override suspend fun addCounselMemo(id: Int, memoText: String): Result<CounselMemo> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.addCounselMemo(id, CounselMemoRequest(memoText = memoText))
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상담 메모 작성 실패 (${response.code()})")
            }
        }
    }

    override suspend fun getCounselMemos(id: Int): Result<List<CounselMemo>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getCounselMemos(id)
            if (response.isSuccessful) {
                response.body()?.map { it.toDomain() } ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상담 메모 목록 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun getCounselLogs(id: Int): Result<List<CounselLog>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getCounselLogs(id)
            if (response.isSuccessful) {
                response.body()?.map { it.toDomain() } ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("상담 상태 변경 이력 조회 실패 (${response.code()})")
            }
        }
    }

    override suspend fun deleteCounsel(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.deleteCounsel(id)
            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string()?.take(200) ?: "없음"
                throw Exception("상담 삭제 실패 (${response.code()}): $errorMsg")
            }
        }
    }
}
