package com.example.flowdesk_android.data.remote

import com.example.flowdesk_android.data.remote.dto.SuperDashboardResponse
import retrofit2.Response
import retrofit2.http.GET

interface SuperApi {

    @GET("/super/dashboard")
    suspend fun getDashboard(): Response<SuperDashboardResponse>
}
