package com.example.flowdesk_android.feature.counsel_management.data.api

import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselDashboardResponse
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselDetailResponse
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselListResponse
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselStatusUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselMemoRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselMemoDto
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselLogDto

interface CounselApi {

    @GET("counsels/dashboard")
    suspend fun getDashboard(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Response<CounselDashboardResponse>

    @GET("counsels")
    suspend fun getCounsels(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("q") query: String?,
        @Query("counselStat") counselStat: Int?,
        @Query("empSeq") empSeq: Int?,
        @Query("webCode") webCode: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("duplicateState") duplicateState: String?,
        @Query("resvStartDate") resvStartDate: String?,
        @Query("resvEndDate") resvEndDate: String?
    ): Response<CounselListResponse>

    @GET("counsels/{id}")
    suspend fun getCounselDetail(
        @Path("id") id: Int
    ): Response<CounselDetailResponse>

    @PATCH("counsels/{id}")
    suspend fun updateCounsel(
        @Path("id") id: Int,
        @Body request: CounselUpdateRequest
    ): Response<CounselDetailResponse>

    @PATCH("counsels/{id}/status")
    suspend fun updateCounselStatus(
        @Path("id") id: Int,
        @Body request: CounselStatusUpdateRequest
    ): Response<Unit>

    @POST("counsels/{id}/memo")
    suspend fun addCounselMemo(
        @Path("id") id: Int,
        @Body request: CounselMemoRequest
    ): Response<CounselMemoDto>

    @GET("counsels/{id}/memo")
    suspend fun getCounselMemos(
        @Path("id") id: Int
    ): Response<List<CounselMemoDto>>

    @GET("counsels/{id}/logs")
    suspend fun getCounselLogs(
        @Path("id") id: Int
    ): Response<List<CounselLogDto>>
}
