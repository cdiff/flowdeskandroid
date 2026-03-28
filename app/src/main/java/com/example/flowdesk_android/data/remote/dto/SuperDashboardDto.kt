package com.example.flowdesk_android.data.remote.dto

data class SuperDashboardResponse(
    val overview: OverviewDto,
    val today: TodayDto,
    val monthlyTrends: MonthlyTrendsDto,
    val security: SecurityDto,
    val tenantStats: List<TenantStatDto>
)

data class OverviewDto(
    val totalTenants: Int,
    val activeTenants: Int,
    val totalUsers: Int,
    val activeUsers: Int,
    val totalCounsels: Int,
    val totalPosts: Int,
    val totalRoles: Int,
    val totalPermissions: Int
)

data class TodayDto(
    val newUsers: Int,
    val newCounsels: Int,
    val newPosts: Int,
    val activeSessions: Int
)

data class MonthlyTrendsDto(
    val userRegistrations: List<MonthlyCountDto>,
    val counselRegistrations: List<MonthlyCountDto>,   // counselCreations → counselRegistrations
    val tenantRegistrations: List<MonthlyCountDto>     // postCreations → tenantRegistrations
)

data class MonthlyCountDto(
    val month: String,  // "2026-03"
    val count: Int
)

data class SecurityDto(
    val totalBlockedIps: Int,
    val totalBlockedHps: Int,
    val totalBlockedWords: Int,
    val recentBlockedIps: Int,
    val recentBlockedHps: Int
)

data class TenantStatDto(
    val tenantId: Int,
    val tenantName: String,
    val isActive: Int,
    val createdAt: String,
    val userCount: Int,
    val activeUserCount: Int,
    val counselCount: Int,
    val todayCounselCount: Int,
    val postCount: Int,
    val roleCount: Int,
    val websiteCount: Int,
    val blockedIpCount: Int,
    val blockedHpCount: Int,
    val blockedWordCount: Int,
    val activeSessionCount: Int
)
