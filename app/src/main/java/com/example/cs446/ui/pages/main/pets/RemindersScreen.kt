package com.example.cs446.ui.pages.main.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ReminderScreen(
    petId: UUID,
    viewModel: RemindersViewModel,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { ReminderRepository() }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(LocalDateTime.now().plusMinutes(1)) }
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val userId by viewModel.currentUserId.collectAsState()

    LaunchedEffect(petId) {
        reminders = repo.getRemindersForPet(petId)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Create reminder
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                userId?.let { uid ->
                    val reminder = Reminder(
                        id = UUID.randomUUID(),
                        createdAt = Instant.now(),
                        petId = petId,
                        userId = uid,
                        title = title,
                        description = description,
                        time = time
                    )
                    scope.launch {
                        repo.addReminder(reminder)
                        // TODO
                        reminders = repo.getRemindersForPet(petId) // Refresh list
                        title = ""
                        description = ""
                        time = LocalDateTime.now().plusMinutes(1)
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Set Reminder")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of reminders
        Text("Upcoming Reminders", style = MaterialTheme.typography.headlineSmall)

        if (reminders.isEmpty()) {
            Text(
                "No reminders set",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(reminders) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        onDelete = {
                            scope.launch {
                                repo.deleteReminder(reminder.id)
                                reminders = repo.getRemindersForPet(petId) // Refresh list
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ReminderItem(reminder: Reminder, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete reminder")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(reminder.description)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Due: ${reminder.time.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}