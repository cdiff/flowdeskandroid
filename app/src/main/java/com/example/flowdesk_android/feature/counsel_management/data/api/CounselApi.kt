package com.example.flowdesk_android.feature.counsel_management.data.api

import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselDashboardResponse
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

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
}
