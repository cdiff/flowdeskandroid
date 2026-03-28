package com.example.flowdesk_android.domain.repository

import com.example.flowdesk_android.data.remote.dto.SuperDashboardResponse

interface SuperRepository {
    suspend fun getDashboard(): Result<SuperDashboardResponse>
}
