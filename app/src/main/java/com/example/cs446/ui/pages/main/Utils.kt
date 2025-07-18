package com.example.cs446.ui.pages.main

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

fun formatDate(instant: Instant, unit: ChronoUnit = ChronoUnit.DAYS): String {
    val formatter = when(unit) {
        ChronoUnit.DAYS -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        ChronoUnit.WEEKS -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        ChronoUnit.MONTHS -> SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        else -> SimpleDateFormat("yyyy", Locale.getDefault())
    }
    val millis = instant.toEpochMilliseconds()
    val date = Date(millis)
    return formatter.format(date)
}

fun calculateAge(birthdate: Instant): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val birthDate = birthdate.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val period = birthDate.periodUntil(today)

    return when {
        period.years > 0 -> "${period.years} Year${if (period.years > 1) "s" else ""}"
        period.months > 0 -> "${period.months} Month${if (period.months > 1) "s" else ""}"
        else -> "${period.days} Day${if (period.days > 1) "s" else ""}"
    }
}

// helper function to calculate relative time
fun getRelativeTime(timestamp: Instant): String {
    val now = Clock.System.now()
    val duration = now - timestamp

    return when {
        duration.inWholeMinutes < 1 -> "now"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}h"
        duration.inWholeDays < 7 -> "${duration.inWholeDays}d"
        else -> "${duration.inWholeDays / 7}w"
    }
}