package com.example.flowdesk_android.feature.super_admin.data.dto

import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats
import com.example.flowdesk_android.feature.super_admin.domain.model.MonthlyCount
import com.example.flowdesk_android.feature.super_admin.domain.model.MonthlyTrends
import com.example.flowdesk_android.feature.super_admin.domain.model.Overview
import com.example.flowdesk_android.feature.super_admin.domain.model.Security
import com.example.flowdesk_android.feature.super_admin.domain.model.TenantStat
import com.example.flowdesk_android.feature.super_admin.domain.model.Today
import com.google.gson.annotations.SerializedName

data class SuperDashboardResponse(
    @SerializedName("overview") val overview: OverviewDto,
    @SerializedName("today") val today: TodayDto,
    @SerializedName("monthlyTrends") val monthlyTrends: MonthlyTrendsDto,
    @SerializedName("security") val security: SecurityDto,
    @SerializedName("tenantStats") val tenantStats: List<TenantStatDto>
) {
    fun toDomain() = DashboardStats(
        overview = overview.toDomain(),
        today = today.toDomain(),
        monthlyTrends = monthlyTrends.toDomain(),
        security = security.toDomain(),
        tenantStats = tenantStats.map { it.toDomain() }
    )
}

data class OverviewDto(
    @SerializedName("totalTenants") val totalTenants: Int,
    @SerializedName("activeTenants") val activeTenants: Int,
    @SerializedName("totalUsers") val totalUsers: Int,
    @SerializedName("activeUsers") val activeUsers: Int,
    @SerializedName("totalCounsels") val totalCounsels: Int,
    @SerializedName("totalPosts") val totalPosts: Int,
    @SerializedName("totalRoles") val totalRoles: Int,
    @SerializedName("totalPermissions") val totalPermissions: Int
) {
    fun toDomain() = Overview(totalTenants, activeTenants, totalUsers, activeUsers, totalCounsels, totalPosts, totalRoles, totalPermissions)
}

data class TodayDto(
    @SerializedName("newUsers") val newUsers: Int,
    @SerializedName("newCounsels") val newCounsels: Int,
    @SerializedName("newPosts") val newPosts: Int,
    @SerializedName("activeSessions") val activeSessions: Int
) {
    fun toDomain() = Today(newUsers, newCounsels, newPosts, activeSessions)
}

data class MonthlyTrendsDto(
    @SerializedName("userRegistrations") val userRegistrations: List<MonthlyCountDto>,
    @SerializedName("counselRegistrations") val counselRegistrations: List<MonthlyCountDto>,
    @SerializedName("tenantRegistrations") val tenantRegistrations: List<MonthlyCountDto>
) {
    fun toDomain() = MonthlyTrends(
        userRegistrations.map { it.toDomain() },
        counselRegistrations.map { it.toDomain() },
        tenantRegistrations.map { it.toDomain() }
    )
}

data class MonthlyCountDto(
    @SerializedName("month") val month: String,
    @SerializedName("count") val count: Int
) {
    fun toDomain() = MonthlyCount(month, count)
}

data class SecurityDto(
    @SerializedName("totalBlockedIps") val totalBlockedIps: Int,
    @SerializedName("totalBlockedHps") val totalBlockedHps: Int,
    @SerializedName("totalBlockedWords") val totalBlockedWords: Int,
    @SerializedName("recentBlockedIps") val recentBlockedIps: Int,
    @SerializedName("recentBlockedHps") val recentBlockedHps: Int
) {
    fun toDomain() = Security(totalBlockedIps, totalBlockedHps, totalBlockedWords, recentBlockedIps, recentBlockedHps)
}

data class TenantStatDto(
    @SerializedName("tenantId") val tenantId: Int,
    @SerializedName("tenantName") val tenantName: String,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("userCount") val userCount: Int,
    @SerializedName("activeUserCount") val activeUserCount: Int,
    @SerializedName("counselCount") val counselCount: Int,
    @SerializedName("todayCounselCount") val todayCounselCount: Int,
    @SerializedName("postCount") val postCount: Int,
    @SerializedName("roleCount") val roleCount: Int,
    @SerializedName("activeSessionCount") val activeSessionCount: Int
) {
    fun toDomain() = TenantStat(tenantId, tenantName, isActive == 1, createdAt, userCount, activeUserCount, counselCount, todayCounselCount, postCount, roleCount, activeSessionCount)
}
