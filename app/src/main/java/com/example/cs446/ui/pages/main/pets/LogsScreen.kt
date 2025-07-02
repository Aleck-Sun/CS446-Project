package com.example.cs446.ui.pages.main.pets

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cs446.backend.data.model.ActivityLog
import com.example.cs446.backend.data.repository.ActivityLogRepository
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.ui.components.pets.ActivityLogComponent
import com.example.cs446.ui.components.pets.ActivityLogForm
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.view.social.FeedViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    petId: String,
    viewModel: FeedViewModel,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showActivityLogModal by remember { mutableStateOf(false) }
    val activityLogRepository = remember { ActivityLogRepository() }
    val userRepository = remember { UserRepository() }
    val petRepository = remember { PetRepository() }

    val context = LocalContext.current
    var activityLogs by remember { mutableStateOf<List<ActivityLog>>(emptyList()) }
    var pet by remember { mutableStateOf<Pet?>(null) }

    fun fetchActivityLogsAndPet() {
        coroutineScope.launch {
            activityLogs = activityLogRepository.getActivityLogsTableForPet(UUID.fromString(petId))
            pet = petRepository.getPet(UUID.fromString(petId))
        }
    }

    // Launch a coroutine to load logs when petId changes
    LaunchedEffect(petId) {
        fetchActivityLogsAndPet()
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(MainActivityDestination.Pets, null) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${pet?.name ?: "Pet"} Activity Logs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

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
                            onSubmit = { activityDate, activityType, comment, makePost, makePublic, imageUris ->
                                coroutineScope.launch {
                                    handleActivitySubmission(
                                        context = context,
                                        viewModel = viewModel,
                                        userRepository = userRepository,
                                        activityLogRepository = activityLogRepository,
                                        activityDate = activityDate,
                                        activityType = activityType,
                                        comment = comment,
                                        makePost = makePost,
                                        makePublic = makePublic,
                                        imageUris = imageUris,
                                        pet = pet,
                                    )
                                    showActivityLogModal = false
                                    fetchActivityLogsAndPet()
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
    context: Context,
    viewModel: FeedViewModel,
    userRepository: UserRepository,
    activityLogRepository: ActivityLogRepository,
    activityDate: Instant,
    activityType: String,
    comment: String,
    makePost: Boolean,
    makePublic: Boolean,
    imageUris: List<Uri>,
    pet: Pet?,
) {
    val userId = userRepository.getCurrentUserId()
    if (userId == null || pet == null) {
        // TODO: This should never happen -- raise error if it does
        return
    }

    if (makePost) {
        val templates = listOf(
            "${pet.name} just did a $activityType!",
            "Look at ${pet.name} go! They did a $activityType!",
            "${pet.name} completed a $activityType!",
            "${pet.name} has completed the $activityType activity!",
            "Another $activityType done by ${pet.name}!",
            "${pet.name} crushed it with a $activityType!"
        )

        val caption = templates.random()
        viewModel.uploadPost(context, pet.id, caption, imageUris, makePublic)
    }
    activityLogRepository.addActivityLog(ActivityLog(
        userId = userId,
        petId = pet.id,
        activityType = activityType,
        comment = comment,
        createdAt = activityDate
    ))
}
