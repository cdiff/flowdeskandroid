package com.example.flowdesk_android.core.extension

import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * UTC ISO-8601 형식(예: 2026-01-26T12:00:00.000Z) 또는 일반 일시 형식을
 * 한국 표준시(KST) 기준 끝에 마침표가 없는 "yyyy. MM. dd" 포맷 문자열로 변환하는 확장 함수
 */
fun String.toFormattedDateString(): String {
    if (this.isBlank()) return ""
    return try {
        // 1. 타임존 정보(Z)가 있는 ISO-8601 형식 파싱 시도
        val instant = Instant.parse(this)
        val zoneId = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREAN)
        instant.atZone(zoneId).format(formatter)
    } catch (e: Exception) {
        try {
            // 2. 타임존이 없는 LocalDateTime 형식 (예: 2026-03-23T12:00:00) 파싱 시도
            val ldt = LocalDateTime.parse(this)
            val formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREAN)
            ldt.format(formatter)
        } catch (ex: Exception) {
            try {
                // 3. 단순 yyyy-MM-dd 형태 파싱 시도
                val date = LocalDate.parse(this.substringBefore("T"))
                val formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREAN)
                date.format(formatter)
            } catch (ex2: Exception) {
                this
            }
        }
    }
}
