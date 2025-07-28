package com.example.cs446.ui.components.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import com.example.cs446.backend.data.model.ActivityLog
import kotlinx.datetime.Clock.System
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate as JavaLocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max


@Composable
fun TrendChart(activityLogs: List<ActivityLog>, modifier: Modifier) {
    var data by remember { mutableStateOf(listOf<Int>()) }
    var fields by remember { mutableStateOf(listOf<java.time.LocalDate>()) }
    var maxCount by remember { mutableIntStateOf(data.maxByOrNull { it.toLong() } ?: 0) }
    var unit by remember { mutableStateOf(ChronoUnit.DAYS) }
    val earliestDate = activityLogs.minOfOrNull { it.createdAt } ?: System.now().toJavaInstant()
    var startDay by remember {
        mutableStateOf(
            earliestDate
                .atZone(ZoneId.of("America/Toronto"))
                .toLocalDate()
        )
    }

    fun setGranularity() {
        startDay = when (unit) {
            ChronoUnit.DAYS -> earliestDate
                .atZone(ZoneId.of("America/Toronto"))
                .truncatedTo(ChronoUnit.DAYS)
                .toLocalDate()
            ChronoUnit.WEEKS -> earliestDate
                .atZone(ZoneId.of("America/Toronto"))
                .with(DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS)
                .toLocalDate()
            ChronoUnit.MONTHS -> earliestDate
                .atZone(ZoneId.of("America/Toronto"))
                .withDayOfMonth(1)
                .truncatedTo(ChronoUnit.DAYS)
                .toLocalDate()
            else -> throw Exception("Invalid time unit")
        }

        var tmpDay = startDay
        val tmpData = mutableListOf<Int>()
        val tmpDates = mutableListOf<java.time.LocalDate>()
        while (
            tmpDay < Instant.now().atZone(ZoneId.of("America/Toronto")).toLocalDate()
        ) {
            val nextDate = when(unit) {
                ChronoUnit.DAYS -> tmpDay
                    .plusDays(1)
                    .toKotlinLocalDate()
                    .toJavaLocalDate()
                ChronoUnit.WEEKS -> tmpDay
                    .plusWeeks(1)
                    .toKotlinLocalDate()
                    .toJavaLocalDate()
                ChronoUnit.MONTHS -> tmpDay
                    .plusMonths(1)
                    .toKotlinLocalDate()
                    .toJavaLocalDate()
                else -> throw Exception("Invalid time unit")
            }

            val count = activityLogs.count {
                val logDate = it.createdAt.atZone(ZoneId.of("America/Toronto")).toLocalDate()
                logDate >= tmpDay &&
                        logDate < nextDate &&
                        logDate.year == JavaLocalDate.now(ZoneId.of("America/Toronto")).year
            }

            if (count > 0) {
                tmpData.add(count)
                tmpDates.add(tmpDay)
            }

            tmpDay = nextDate
        }

        data = tmpData
        fields = tmpDates
        maxCount = max(tmpData.maxOrNull()?:1, 1)
    }
    LaunchedEffect(unit, activityLogs) {
        setGranularity()
    }

    val gradientA = Color(0xFFCEB8FF)
    val gradientB = Color(0xFFE6D6FF)

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { unit = ChronoUnit.DAYS }
            ) {
                Icon(Icons.Default.CalendarViewDay, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Day")
            }
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { unit = ChronoUnit.WEEKS }
            ) {
                Icon(Icons.Default.CalendarViewWeek, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Week")
            }
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { unit = ChronoUnit.MONTHS }
            ) {
                Icon(Icons.Default.CalendarViewMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Month")
            }
        }

        LazyColumn {
            itemsIndexed(data) { idx, value ->
                val date = fields[idx]
                val dateText = when(unit) {
                    ChronoUnit.MONTHS -> date.month.name.substring(0,3).lowercase().replaceFirstChar { it.uppercase() }
                    else -> "${date.month.name.substring(0,3).lowercase().replaceFirstChar { it.uppercase() }}, ${date.dayOfMonth}"
                }
                if (idx == 0 || date.year != fields[idx-1].year) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = date.year.toString(),
                            modifier = Modifier.width(80.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text=dateText,
                        modifier = Modifier.width(80.dp)
                    )
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width((value.toInt() * 200 / maxCount.toInt()).dp)
                            .background(
                                if (idx%2 == 0) {
                                    gradientA
                                } else {
                                    gradientB
                                }
                            )
                    )
                    Text(" $value")
                }
            }
        }
//        Spacer(modifier = Modifier.weight(1f))
//        Row (
//            modifier = Modifier.fillMaxWidth()
//                .padding(top=16.dp)
//        ){
//            Button(
//                modifier = Modifier.padding(end = 8.dp),
//                onClick = {
//                    unit = ChronoUnit.DAYS
//                    setGranularity()
//                }
//            ) {
//                Icon(Icons.Default.CalendarViewDay, contentDescription = null)
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Day")
//            }
//            Button(
//                modifier = Modifier.padding(end = 8.dp),
//                onClick = {
//                    unit = ChronoUnit.WEEKS
//                    setGranularity()
//                }
//            ) {
//                Icon(Icons.Default.CalendarViewWeek, contentDescription = null)
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Week")
//            }
//            Button(
//                modifier = Modifier.padding(end = 8.dp),
//                onClick = {
//                    unit = ChronoUnit.MONTHS
//                    setGranularity()
//                }
//            ) {
//                Icon(Icons.Default.CalendarViewMonth, contentDescription = null)
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Month")
//            }
//        }
    }
}
