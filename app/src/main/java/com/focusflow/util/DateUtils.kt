package com.focusflow.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Long.toLocalDate(): LocalDate {
    return java.time.Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

fun todayEpoch(): Long {
    return LocalDate.now().toEpochMillis()
}

fun todayStart(): Long {
    return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun todayEnd(): Long {
    return LocalDate.now().atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun weekStart(): Long {
    val today = LocalDate.now()
    val startOfWeek = today.minusDays((today.dayOfWeek.value - 1).toLong())
    return startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun weekEnd(): Long {
    val today = LocalDate.now()
    val endOfWeek = today.plusDays((7 - today.dayOfWeek.value).toLong())
    return endOfWeek.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun monthStart(): Long {
    return LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun monthEnd(): Long {
    return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        .atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Long.toFormattedString(): String {
    val localDate = this.toLocalDate()
    return String.format("%04d-%02d-%02d", localDate.year, localDate.monthValue, localDate.dayOfMonth)
}
