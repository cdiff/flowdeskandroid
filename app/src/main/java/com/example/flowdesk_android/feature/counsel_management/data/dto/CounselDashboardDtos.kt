package com.example.flowdesk_android.feature.counsel_management.data.dto

import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDashboard
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselSummary
import com.example.flowdesk_android.feature.counsel_management.domain.model.DailyTrend
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.HourlyDistribution
import com.example.flowdesk_android.feature.counsel_management.domain.model.TopWebsite
import com.example.flowdesk_android.feature.counsel_management.domain.model.UpcomingReservation
import com.google.gson.annotations.SerializedName

// ── 루트 응답 ──────────────────────────────────────────────
data class CounselDashboardResponse(
    @SerializedName("summary")               val summary: SummaryDto,
    @SerializedName("statusDistribution")    val statusDistribution: List<StatusDistributionDto>?,
    @SerializedName("employeeStats")         val employeeStats: List<EmployeeStatDto>?,
    @SerializedName("dailyTrends")           val dailyTrends: List<DailyTrendDto>?,
    @SerializedName("topWebsites")           val topWebsites: List<TopWebsiteDto>?,
    @SerializedName("hourlyDistribution")    val hourlyDistribution: List<HourlyDistributionDto>?,
    @SerializedName("upcomingReservations")  val upcomingReservations: List<UpcomingReservationDto>?
) {
    fun toDomain() = CounselDashboard(
        summary              = summary.toDomain(),
        statusDistribution   = statusDistribution?.map { it.toDomain() } ?: emptyList(),
        employeeStats        = employeeStats?.map { it.toDomain() } ?: emptyList(),
        dailyTrends          = dailyTrends?.map { it.toDomain() } ?: emptyList(),
        topWebsites          = topWebsites?.map { it.toDomain() } ?: emptyList(),
        hourlyDistribution   = hourlyDistribution?.map { it.toDomain() } ?: emptyList(),
        upcomingReservations = upcomingReservations?.map { it.toDomain() } ?: emptyList()
    )
}

// ── 요약 ──────────────────────────────────────────────────
data class SummaryDto(
    @SerializedName("totalCounsels")     val totalCounsels: Int = 0,
    @SerializedName("newCounsels")       val newCounsels: Int = 0,
    @SerializedName("completedCounsels") val completedCounsels: Int = 0,
    @SerializedName("completionRate")    val completionRate: Double = 0.0
) {
    fun toDomain() = CounselSummary(totalCounsels, newCounsels, completedCounsels, completionRate)
}

// ── 상태별 분포 ───────────────────────────────────────────
data class StatusDistributionDto(
    @SerializedName("counselStat")  val counselStat: Int = 0,
    @SerializedName("statusName")   val statusName: String = "",
    @SerializedName("color")        val color: String = "#64748B",
    @SerializedName("count")        val count: Int = 0
) {
    fun toDomain() = CounselStatusStat(counselStat, statusName, color, count)
}

// ── 담당자별 현황 ─────────────────────────────────────────
data class EmployeeStatDto(
    @SerializedName("empSeq")  val empSeq: Int = 0,
    @SerializedName("empName") val empName: String = "",
    @SerializedName("count")   val count: Int = 0
) {
    fun toDomain() = EmployeeStat(empSeq, empName, count)
}

// ── 일별 추이 ─────────────────────────────────────────────
data class DailyTrendDto(
    @SerializedName("date")  val date: String = "",
    @SerializedName("count") val count: Int = 0
) {
    fun toDomain() = DailyTrend(date, count)
}

// ── 웹사이트 Top 5 ────────────────────────────────────────
data class TopWebsiteDto(
    @SerializedName("webCode")  val webCode: String = "",
    @SerializedName("webTitle") val webTitle: String = "",
    @SerializedName("count")    val count: Int = 0
) {
    fun toDomain() = TopWebsite(webCode, webTitle, count)
}

// ── 시간대별 분포 ─────────────────────────────────────────
data class HourlyDistributionDto(
    @SerializedName("hour")  val hour: Int = 0,
    @SerializedName("count") val count: Int = 0
) {
    fun toDomain() = HourlyDistribution(hour, count)
}

// ── 예정된 예약 ───────────────────────────────────────────
data class UpcomingReservationDto(
    @SerializedName("counselSeq")      val counselSeq: Int = 0,
    @SerializedName("name")            val name: String = "",
    @SerializedName("counselHp")       val counselHp: String = "",
    @SerializedName("counselResvDtm")  val counselResvDtm: String = "",
    @SerializedName("empName")         val empName: String? = null,
    @SerializedName("counselStat")     val counselStat: Int = 0,
    @SerializedName("statusName")      val statusName: String = ""
) {
    fun toDomain() = UpcomingReservation(counselSeq, name, counselHp, counselResvDtm, empName, counselStat, statusName)
}
