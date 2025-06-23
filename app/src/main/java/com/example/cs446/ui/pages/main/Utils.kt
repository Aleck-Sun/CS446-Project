package com.example.cs446.ui.pages.main

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(instant: Instant): String {
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val millis = instant.toEpochMilliseconds()
    val date = Date(millis)
    return formatter.format(date)
}

fun calculateAge(birthdate: Instant): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val birthDate = birthdate.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val period = birthDate.periodUntil(today)

    return when {
        period.years > 0 -> "${period.years} year${if (period.years > 1) "s" else ""}"
        period.months > 0 -> "${period.months} month${if (period.months > 1) "s" else ""}"
        else -> "${period.days} day${if (period.days > 1) "s" else ""}"
    }
}