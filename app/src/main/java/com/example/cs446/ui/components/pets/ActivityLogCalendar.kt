package com.example.cs446.ui.components.pets

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cs446.backend.data.model.ActivityLog
import com.example.cs446.ui.theme.CS446Theme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityLogCalendar(
    activityLogs: List<ActivityLog>,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val logsByDate = remember(activityLogs) {
        activityLogs.groupBy { log ->
            log.createdAt.atZone(ZoneId.of("America/Toronto")).toLocalDate()
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // month navigation header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
            }
            
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
            }
        }
        
        // days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // grid
        CalendarGrid(
            yearMonth = currentMonth,
            logsByDate = logsByDate,
            onDateClick = { date -> selectedDate = date }
        )
        
        if (selectedDate != null) {
            val logsForDate = logsByDate[selectedDate] ?: emptyList()
            ActivityLogsForDate(
                date = selectedDate!!,
                logs = logsForDate,
                onClose = { selectedDate = null }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    logsByDate: Map<LocalDate, List<ActivityLog>>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    
    val calendarCells = mutableListOf<CalendarCell>()
    
    repeat(firstDayOfWeek) {
        calendarCells.add(CalendarCell.Empty)
    }
    
    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        val hasLogs = logsByDate.containsKey(date)
        val logCount = logsByDate[date]?.size ?: 0
        val firstActivity = logsByDate[date]?.firstOrNull()
        calendarCells.add(CalendarCell.Day(date, hasLogs, logCount, firstActivity))
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(calendarCells) { cell ->
            when (cell) {
                is CalendarCell.Empty -> {
                    Box(
                        modifier = Modifier
                            .height(70.dp)
                            .padding(2.dp)
                    )
                }
                is CalendarCell.Day -> {
                    CalendarDayCell(
                        date = cell.date,
                        hasLogs = cell.hasLogs,
                        logCount = cell.logCount,
                        firstActivity = cell.firstActivity,
                        onClick = { onDateClick(cell.date) }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDayCell(
    date: LocalDate,
    hasLogs: Boolean,
    logCount: Int,
    firstActivity: ActivityLog?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(70.dp)
            .padding(2.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasLogs) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (hasLogs) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // date and count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = if (hasLogs) FontWeight.Bold else FontWeight.Normal,
                    color = if (hasLogs) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.onSurface
                )
                
                if (hasLogs && logCount > 1) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = logCount.toString(),
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (hasLogs && firstActivity != null) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = firstActivity.activityType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasLogs) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (logCount > 1) {
                        Text(
                            text = "+ ${logCount - 1} more",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ActivityLogsForDate(
    date: LocalDate,
    logs: List<ActivityLog>,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Activities for ${date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = onClose) {
                        Text("Close")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activities logged for this date",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { log ->
                            ActivityLogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ActivityLogItem(log: ActivityLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.activityType,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = log.createdAt.atZone(ZoneId.of("America/Toronto"))
                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (log.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.comment,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private sealed class CalendarCell {
    object Empty : CalendarCell()
    data class Day(
        val date: LocalDate,
        val hasLogs: Boolean,
        val logCount: Int,
        val firstActivity: ActivityLog?
    ) : CalendarCell()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ActivityLogCalendarPreview() {
    CS446Theme {
        ActivityLogCalendar(
            activityLogs = emptyList(),
            modifier = Modifier.fillMaxSize()
        )
    }
} 