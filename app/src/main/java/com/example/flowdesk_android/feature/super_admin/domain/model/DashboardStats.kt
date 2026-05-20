package com.example.flowdesk_android.feature.super_admin.domain.model

data class DashboardStats(
    val overview: Overview,
    val today: Today,
    val monthlyTrends: MonthlyTrends,
    val security: Security,
    val tenantStats: List<TenantStat>
)

data class Overview(
    val totalTenants: Int,
    val activeTenants: Int,
    val totalUsers: Int,
    val activeUsers: Int,
    val totalCounsels: Int,
    val totalPosts: Int,
    val totalRoles: Int,
    val totalPermissions: Int
)

data class Today(
    val newUsers: Int,
    val newCounsels: Int,
    val newPosts: Int,
    val activeSessions: Int
)

data class MonthlyTrends(
    val userRegistrations: List<MonthlyCount>,
    val counselRegistrations: List<MonthlyCount>,
    val tenantRegistrations: List<MonthlyCount>
)

data class MonthlyCount(val month: String, val count: Int)

data class Security(
    val totalBlockedIps: Int,
    val totalBlockedHps: Int,
    val totalBlockedWords: Int,
    val recentBlockedIps: Int,
    val recentBlockedHps: Int
)

data class TenantStat(
    val tenantId: Int,
    val tenantName: String,
    val isActive: Boolean,
    val createdAt: String,
    val userCount: Int,
    val activeUserCount: Int,
    val counselCount: Int,
    val todayCounselCount: Int,
    val postCount: Int,
    val roleCount: Int,
    val activeSessionCount: Int
)
