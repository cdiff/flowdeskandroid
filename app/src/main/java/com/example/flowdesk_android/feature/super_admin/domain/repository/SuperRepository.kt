package com.example.flowdesk_android.feature.super_admin.domain.repository

import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats

interface SuperRepository {
    suspend fun getDashboard(): Result<DashboardStats>
}
