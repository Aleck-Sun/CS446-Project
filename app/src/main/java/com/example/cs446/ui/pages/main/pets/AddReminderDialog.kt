package com.example.cs446.ui.pages.main.pets

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, description: String, time: LocalDateTime, repeatIntervalDays: Int, repeatTimes: Int) -> Unit
) {
    val context = LocalContext.current
//    val colorScheme = MaterialTheme.colorScheme

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().plusMinutes(1)) }
    var repeatIntervalDays by remember { mutableStateOf("1") }
    var repeatTimes by remember { mutableStateOf("1") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            false
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Validate inputs
                val time = LocalDateTime.of(selectedDate, selectedTime)
                val repeatEvery = repeatIntervalDays.toIntOrNull() ?: -1
                val timesCount = repeatTimes.toIntOrNull() ?: -1

                if (title.isBlank()) {
                    errorMessage = "Title cannot be empty"
                } else if (time.isBefore(LocalDateTime.now())) {
                    errorMessage = "Reminder time must be in the future"
                } else if (repeatEvery <= 0) {
                    errorMessage = "Repeat every days must be > 0"
                } else if (timesCount <= 0) {
                    errorMessage = "Repeat times must be > 0"
                } else {
                    onAdd(title, description, time, repeatIntervalDays.toInt(), repeatTimes.toInt())
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Create New Reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = selectedDate.format(DateTimeFormatter.ofPattern("yy-MM-dd")),
                        onValueChange = {},
                        label = { Text("Date") },
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                datePickerDialog.updateDate(
                                    selectedDate.year,
                                    selectedDate.monthValue - 1,
                                    selectedDate.dayOfMonth
                                )
                                datePickerDialog.show()
                            }
                    )

                    OutlinedTextField(
                        value = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        onValueChange = {},
                        label = { Text("Time") },
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Select time",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                timePickerDialog.updateTime(
                                    selectedTime.hour,
                                    selectedTime.minute
                                )
                                timePickerDialog.show()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    OutlinedTextField(
                        value = repeatIntervalDays,
                        onValueChange = { repeatIntervalDays = it.filter { c -> c.isDigit() } },
                        label = { Text("Repeat every X days") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repeatTimes,
                        onValueChange = { repeatTimes = it.filter { c -> c.isDigit() } },
                        label = { Text("Repeat X times") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}