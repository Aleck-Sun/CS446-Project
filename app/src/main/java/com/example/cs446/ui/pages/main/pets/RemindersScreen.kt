package com.example.cs446.ui.pages.main.pets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs446.backend.data.model.Reminder
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
    val userPetRelations by viewModel.userPetRelations.collectAsState()

    val userId by viewModel.currentUserId.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // partition reminders by past and upcoming
    val (upcomingReminders, pastReminders) = remember(reminders) {
        viewModel.getPartitionedReminders()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var reminderBeingEdited by remember { mutableStateOf<Reminder?>(null) }

    val currentTime by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(1000)
        }
    }

    val canSetReminders by remember(userPetRelations, petId) {
        derivedStateOf {
            userPetRelations.find { it.petId == petId }?.permissions?.setReminders == true
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
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Manage Reminders",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )
                IconButton(
                    onClick = { onNavigate(MainActivityDestination.Pets, null) },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

//            Text(
//                "Current time: ${currentTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a"))}",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )

            if (canSetReminders) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.align(Alignment.Start) // left aligned
                ) {
                    Text("Create New Reminder")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Upcoming Reminders
//            Text("Upcoming Reminders", style = MaterialTheme.typography.headlineSmall)

            if (upcomingReminders.isEmpty()) {
                Text("No upcoming reminders")
            } else {
                LazyColumn(
//                    modifier = Modifier
//                        .padding(paddingValues)
//                        .fillMaxSize()
//                        .padding(16.dp)
                    modifier = Modifier.heightIn(max = 700.dp)
                ) {
                    items(upcomingReminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onToggleActive = { isActive ->
                                viewModel.toggleActiveReminder(context, reminder.id, petId)
                            },
                            onEdit = { reminderBeingEdited = it },
                            onDelete = {
                                viewModel.deleteReminder(reminder.id, petId)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }

//            Spacer(modifier = Modifier.height(16.dp))

            // Past Reminders section
//            Text("Past Reminders", style = MaterialTheme.typography.headlineSmall)
//
//            if (pastReminders.isEmpty()) {
//                Text("No past reminders")
//            } else {
//                LazyColumn(
//                    modifier = Modifier.heightIn(max = 400.dp) // Constrain height
//                ) {
//                    items(pastReminders) { reminder ->
//                        ReminderItem(reminder = reminder)
//                        HorizontalDivider()
//                    }
//                }
//            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description, time, repeatIntervalDays, repeatTimes ->
                viewModel.addReminder(
                    context,
                    petId,
                    title,
                    description,
                    time,
                    repeatIntervalDays,
                    repeatTimes
                )
                showAddDialog = false
            }
        )
    }

    reminderBeingEdited?.let { reminder ->
        EditReminderDialog(
            reminder = reminder,
            onDismiss = { reminderBeingEdited = null },
            onEdit = { title, description, time ->
                viewModel.updateReminder(
                    context = context,
                    reminderId = reminder.id,
                    petId = petId,
                    title = title,
                    description = description,
                    time = time
                )
                reminderBeingEdited = null
            }
        )
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggleActive: ((Boolean) -> Unit)? = null,
    onEdit: ((Reminder) -> Unit)? = null,
    onDelete: ((Reminder) -> Unit)? = null
) {
    val systemZone = ZoneId.of("America/Toronto")
    val now = LocalDateTime.now(systemZone)
    val isUpcoming = reminder.time
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onToggleActive != null && isUpcoming) {
                        Switch(
                            checked = isActive,
                            onCheckedChange = onToggleActive,
                            enabled = true,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    if (onEdit != null) {
                        IconButton(
                            onClick = { onEdit(reminder) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Reminder",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(onClick = { onDelete(reminder) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Reminder",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Due: ${reminder.time.withZoneSameInstant(ZoneId.of("America/Toronto"))
                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))}",
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