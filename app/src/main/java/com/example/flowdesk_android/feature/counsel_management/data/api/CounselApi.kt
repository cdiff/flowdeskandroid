package com.example.flowdesk_android.feature.counsel_management.data.api

import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselDashboardResponse
import retrofit2.Response
import retrofit2.http.GET

import retrofit2.http.Query

interface CounselApi {

    @GET("counsels/dashboard")
    suspend fun getDashboard(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Response<CounselDashboardResponse>
}
