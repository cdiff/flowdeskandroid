package com.example.flowdesk_android.core.extension

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * UTC ISO-8601 형식의 날짜 문자열을 한국 표준시 포맷의 문자열로 변환하는 확장 함수
 */
fun String.toFormattedDateString(): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(this)
        if (date != null) {
            SimpleDateFormat("yyyy. MM. dd. a hh:mm", Locale.KOREA).apply {
                timeZone = TimeZone.getDefault()
            }.format(date)
        } else {
            this
        }
    } catch (e: Exception) {
        this
    }
}
