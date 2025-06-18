package com.example.cs446.ui.pages.main

import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(instant: Instant): String {
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val millis = instant.toEpochMilliseconds()
    val date = Date(millis)
    return formatter.format(date)
}
