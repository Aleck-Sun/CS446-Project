package com.example.cs446.ui.pages.main.pets

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cs446.backend.data.model.ActivityLog
import com.example.cs446.backend.data.repository.ActivityLogRepository
import com.example.cs446.data.model.Pet
import com.example.cs446.ui.components.ActivityLogComponent
import com.example.cs446.ui.components.ActivityLogForm
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.ui.theme.CS446Theme
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    petId: String,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
    // TODO: get pet using petId
    var pet = Pet(
        UUID.fromString(petId),
        kotlinx.datetime.Instant.parse("2021-02-03T00:00:00Z"),
        "Charlie",
        1, "Golden Retriever",
        UUID.randomUUID(),
        kotlinx.datetime.Instant.parse("2025-05-28T00:00:00Z"),
        65.0
    )

    val coroutineScope = rememberCoroutineScope()
    var showActivityLogModal by remember { mutableStateOf(false) }
    val activityLogRepository = remember { ActivityLogRepository() }

    // State to hold loaded activity logs
    var activityLogs by remember { mutableStateOf<List<ActivityLog>>(emptyList()) }

    // Launch a coroutine to load logs when petId changes
    LaunchedEffect(petId) {
        activityLogs = activityLogRepository.getActivityLogsTableForPet(UUID.fromString(petId))
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${pet?.name ?: "Pet"} Activity Logs",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(activityLogs) { log ->
                    ActivityLogComponent(activityLog = log)
                }
            }
        }

        // Floating Action Button in bottom right
        FloatingActionButton(
            onClick = { showActivityLogModal = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Activity Log"
            )
        }

        if (showActivityLogModal) {
            Dialog(
                onDismissRequest = { showActivityLogModal = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add Activity Log",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { showActivityLogModal = false }
                            ) {
                                Text("Cancel")
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        ActivityLogForm(
                            onSubmit = { activityDate, activityType, comment, makePost, imageUri ->
                                coroutineScope.launch {
                                    handleActivitySubmission(
                                        activityLogRepository = activityLogRepository,
                                        activityDate = activityDate,
                                        activityType = activityType,
                                        comment = comment,
                                        makePost = makePost,
                                        imageUri = imageUri,
                                        petId = UUID.fromString(petId)
                                    )
                                    showActivityLogModal = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private suspend fun handleActivitySubmission(
    activityLogRepository: ActivityLogRepository,
    activityDate: Instant,
    activityType: String,
    comment: String,
    makePost: Boolean,
    imageUri: Uri?,
    petId: UUID,
) {
    if (makePost) {
        // TODO: Implement posting logic
    }
    activityLogRepository.addActivityLog(ActivityLog(
        userId = UUID.randomUUID(), // TODO: Have a way to globally get current user Id
        petId = petId,
        activityType = activityType,
        comment = comment,
        createdAt = activityDate
    ))
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun LogsScreenPreview() {
    CS446Theme {
        LogsScreen(
            petId = "",
            onNavigate = { } as (MainActivityDestination, String?) -> Unit
        )
    }
}
