package com.example.flowdesk_android.feature.counsel_management.domain.model

data class CounselDashboard(
    val summary: CounselSummary,
    val statusDistribution: List<CounselStatusStat>,
    val employeeStats: List<EmployeeStat>,
    val dailyTrends: List<DailyTrend>,
    val topWebsites: List<TopWebsite>,
    val hourlyDistribution: List<HourlyDistribution>,
    val upcomingReservations: List<UpcomingReservation>
)

data class CounselSummary(
    val totalCounsels: Int,
    val newCounsels: Int,
    val completedCounsels: Int,
    val completionRate: Double
)

data class CounselStatusStat(
    val counselStat: Int,
    val statusName: String,
    val color: String,
    val count: Int
)

data class EmployeeStat(
    val empSeq: Int,
    val empName: String,
    val count: Int
)

data class DailyTrend(
    val date: String,
    val count: Int
)

data class TopWebsite(
    val webCode: String,
    val webTitle: String,
    val count: Int
)

data class HourlyDistribution(
    val hour: Int,
    val count: Int
)

data class UpcomingReservation(
    val counselSeq: Int,
    val name: String,
    val counselHp: String,
    val counselResvDtm: String,
    val empName: String?,
    val counselStat: Int,
    val statusName: String
)
