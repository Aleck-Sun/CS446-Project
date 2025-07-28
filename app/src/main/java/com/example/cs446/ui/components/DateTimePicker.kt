package com.example.cs446.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimePickerTextField(
    dateTime: LocalDateTime,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    onDateTimeChange: (LocalDateTime) -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val datePickerDialog = remember(colorScheme) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                val newDateTime = LocalDateTime.of(newDate, dateTime.toLocalTime())
                onDateTimeChange(newDateTime)
            },
            dateTime.year,
            dateTime.monthValue - 1,
            dateTime.dayOfMonth
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setOnShowListener {
                    val buttonPositive = getButton(DatePickerDialog.BUTTON_POSITIVE)
                    val buttonNegative = getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    
                    buttonPositive?.setTextColor(colorScheme.primary.toArgb())
                    buttonNegative?.setTextColor(colorScheme.primary.toArgb())
                    
                    val datePicker = datePicker
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            val headerViewId = context.resources.getIdentifier("date_picker_header", "id", "android")
                            val headerView = datePicker.findViewById<android.view.View>(headerViewId)
                            headerView?.setBackgroundColor(colorScheme.primary.toArgb())
                        } catch (e: Exception) {
                        }
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val colorStateList = ColorStateList.valueOf(colorScheme.primary.toArgb())
                                
                                val dayPickerViewId = context.resources.getIdentifier("day_picker_view_pager", "id", "android")
                                val dayPickerView = datePicker.findViewById<android.view.View>(dayPickerViewId)
                                val monthViewId = context.resources.getIdentifier("month_view", "id", "android")
                                val monthView = datePicker.findViewById<android.view.View>(monthViewId)
                                
                                dayPickerView?.backgroundTintList = colorStateList
                                monthView?.backgroundTintList = colorStateList
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            }
        }
    }

    val timePickerDialog = remember(colorScheme) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newTime = LocalTime.of(hourOfDay, minute)
                val newDateTime = LocalDateTime.of(dateTime.toLocalDate(), newTime)
                onDateTimeChange(newDateTime)
            },
            dateTime.hour,
            dateTime.minute,
            true // 24-hour format
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setOnShowListener {
                    val buttonPositive = getButton(TimePickerDialog.BUTTON_POSITIVE)
                    val buttonNegative = getButton(TimePickerDialog.BUTTON_NEGATIVE)
                    
                    buttonPositive?.setTextColor(colorScheme.primary.toArgb())
                    buttonNegative?.setTextColor(colorScheme.primary.toArgb())
                }
            }
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Date picker
            OutlinedTextField(
                value = dateTime.toLocalDate().format(dateFormatter),
                onValueChange = {},
                label = { Text("Date") },
                enabled = false,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange, 
                        contentDescription = "Select date",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = colorScheme.onSurface,
                    disabledBorderColor = colorScheme.outline,
                    disabledLabelColor = colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        datePickerDialog.updateDate(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth)
                        datePickerDialog.show()
                    }
            )

            // Time picker
            OutlinedTextField(
                value = dateTime.toLocalTime().format(timeFormatter),
                onValueChange = {},
                label = { Text("Time") },
                enabled = false,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Schedule, 
                        contentDescription = "Select time",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = colorScheme.onSurface,
                    disabledBorderColor = colorScheme.outline,
                    disabledLabelColor = colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        timePickerDialog.updateTime(dateTime.hour, dateTime.minute)
                        timePickerDialog.show()
                    }
            )
        }
    }
} 