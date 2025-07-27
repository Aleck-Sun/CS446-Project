package com.example.cs446.ui.pages.main.pets

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

@Composable
fun ReminderScreen(
    petId: UUID,
    viewModel: RemindersViewModel,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(LocalDateTime.now().plusMinutes(1)) }

    val userId by viewModel.currentUserId.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // partition reminders by past and upcoming
    val (upcomingReminders, pastReminders) = remember(reminders) {
        viewModel.getPartitionedReminders()
    }

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

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Create New Reminder",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.addReminder(
                                context = context,
                                petId = petId,
                                title = title,
                                description = description,
                                time = time
                            )
                            title = ""
                            description = ""
                            time = LocalDateTime.now().plusMinutes(1)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Add Reminder")
                    }
                }
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
    val reminderInstant = reminder.time.atZone(systemZone).toInstant()
    val nowInstant = now.atZone(systemZone).toInstant()

    val isUpcoming = reminderInstant.isAfter(nowInstant) && reminder.active
    val isPast = !isUpcoming

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!reminder.active || isPast)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
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
                        color = if (!reminder.active || isPast)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        reminder.description,
                        color = if (!reminder.active || isPast)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (onToggleActive != null) {
                    Switch(
                        checked = reminder.active,
                        onCheckedChange = onToggleActive,
                        enabled = !isPast,
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
                    color = if (!reminder.active || isPast)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                if (!reminder.active) {
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