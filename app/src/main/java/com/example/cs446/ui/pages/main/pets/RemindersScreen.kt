package com.example.cs446.ui.pages.main.pets

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs446.backend.data.model.Reminder
import com.example.cs446.backend.data.repository.ReminderRepository
import com.example.cs446.ui.components.pets.ReminderScheduler
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.view.pets.RemindersViewModel
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.foundation.clickable
import java.time.Instant
import com.example.cs446.ui.components.DatePickerTextField
import kotlinx.coroutines.selects.select

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    petId: UUID,
    viewModel: RemindersViewModel,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val userId by viewModel.currentUserId.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // partition reminders by past and upcoming
    val (upcomingReminders, pastReminders) = remember(reminders) {
        viewModel.getPartitionedReminders()
    }

    var showAddDialog by remember { mutableStateOf(false) }


    val currentTime by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(1000)
        }
    }

    LaunchedEffect(petId) {
        viewModel.loadRemindersForPet(petId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Manage Reminders",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                "Current time: ${currentTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.Start) // left aligned
            ) {
                Text("Create New Reminder")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Reminders
            Text("Upcoming Reminders", style = MaterialTheme.typography.headlineSmall)

            if (upcomingReminders.isEmpty()) {
                Text("No upcoming reminders")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp) // Constrain height
                ) {
                    items(upcomingReminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onToggleActive = { isActive ->
                                viewModel.toggleActiveReminder(context, reminder.id, petId)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Past Reminders section
            Text("Past Reminders", style = MaterialTheme.typography.headlineSmall)

            if (pastReminders.isEmpty()) {
                Text("No past reminders")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp) // Constrain height
                ) {
                    items(pastReminders) { reminder ->
                        ReminderItem(reminder = reminder)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggleActive: ((Boolean) -> Unit)? = null
) {
    val systemZone = ZoneId.systemDefault()
    val now = LocalDateTime.now(systemZone)
    val isUpcoming = reminder.time.atZone(systemZone)
        .isAfter(now.atZone(systemZone))
    val isActive = reminder.active

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isUpcoming -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            !isUpcoming -> MaterialTheme.colorScheme.onSurfaceVariant
                            !isActive -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        reminder.description,
                        color = when {
                            !isUpcoming -> MaterialTheme.colorScheme.onSurfaceVariant
                            !isActive -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                if (onToggleActive != null && isUpcoming) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = onToggleActive,
                        enabled = isUpcoming,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Due: ${reminder.time.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        !isUpcoming -> MaterialTheme.colorScheme.onSurfaceVariant
                        !isActive -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (!isActive) {
                    Text(
                        "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}