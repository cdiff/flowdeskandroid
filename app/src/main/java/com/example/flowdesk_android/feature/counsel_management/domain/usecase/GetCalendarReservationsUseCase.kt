package com.example.flowdesk_android.feature.counsel_management.domain.usecase

import com.example.flowdesk_android.feature.counsel_management.domain.model.CalendarReservationData
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselItem
import com.example.flowdesk_android.feature.counsel_management.domain.repository.CounselRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetCalendarReservationsUseCase @Inject constructor(
    private val counselRepository: CounselRepository
) {
    suspend operator fun invoke(
        month: YearMonth,
        empSeq: Int?
    ): Result<CalendarReservationData> {
        // 1. Grid 범위 계산 (Sunday overflow & Saturday overflow)
        val firstDay = month.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 0=Sunday, 1=Monday ... 6=Saturday
        val gridStartDate = firstDay.minusDays(firstDayOfWeek.toLong())

        val lastDay = month.atEndOfMonth()
        val lastDayOfWeek = lastDay.dayOfWeek.value % 7
        val gridEndDate = lastDay.plusDays((6 - lastDayOfWeek).toLong())

        // 2. API 호출 및 가공
        return counselRepository.getCounsels(
            limit = 1000,
            resvStartDate = gridStartDate.toString(),
            resvEndDate = gridEndDate.toString(),
            empSeq = empSeq
        ).map { counselList ->
            // LocalDate 기반 그룹화
            val mapped = counselList.items
                .filter { !it.counselResvDtm.isNullOrBlank() }
                .groupBy { parseLocalDate(it.counselResvDtm)!! }

            // 현재 선택 월의 예약 건수만 카운트
            val thisMonthCount = counselList.items.count { item ->
                val date = parseLocalDate(item.counselResvDtm)
                date != null && date.year == month.year && date.monthValue == month.monthValue
            }

            CalendarReservationData(
                reservations = mapped,
                monthlyCount = thisMonthCount
            )
        }
    }

    private fun parseLocalDate(dtm: String?): LocalDate? {
        if (dtm.isNullOrBlank()) return null
        return try {
            val dateStr = if (dtm.contains("T")) dtm.substringBefore("T") else dtm.substringBefore(" ")
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
