package com.example.flowdesk_android.feature.super_admin.data.api

import com.example.flowdesk_android.feature.super_admin.data.dto.SuperDashboardResponse
import retrofit2.Response
import retrofit2.http.GET

interface SuperApi {

    @GET("super/dashboard")
    suspend fun getDashboard(): Response<SuperDashboardResponse>
}
