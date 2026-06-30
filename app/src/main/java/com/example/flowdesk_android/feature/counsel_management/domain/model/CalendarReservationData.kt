package com.example.flowdesk_android.feature.counsel_management.domain.model

import java.time.LocalDate

data class CalendarReservationData(
    val reservations: Map<LocalDate, List<CounselItem>>,
    val monthlyCount: Int
)
