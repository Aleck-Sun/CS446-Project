package com.example.cs446.ui.pages.main.pets

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import com.example.cs446.backend.data.model.ActivityLog
import com.example.cs446.backend.data.model.ActivityLogType
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.ActivityLogRepository
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.ui.components.pets.ActivityLogCalendar
import com.example.cs446.ui.components.pets.ActivityLogComponent
import com.example.cs446.ui.components.pets.ActivityLogForm
import com.example.cs446.ui.components.pets.TrendChart
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.view.social.FeedViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

enum class LogViewMode {
    LIST, CALENDAR, CHART
}

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
    var viewMode by remember { mutableStateOf(LogViewMode.LIST) }
    var searchText by remember { mutableStateOf("") }
    val activityLogRepository = remember { ActivityLogRepository() }
    val userRepository = remember { UserRepository() }
    val petRepository = remember { PetRepository() }

    val context = LocalContext.current
    var activityLogs by remember { mutableStateOf<List<ActivityLog>>(emptyList()) }
    var pet by remember { mutableStateOf<Pet?>(null) }
    var activityTypes by remember {mutableStateOf<List<ActivityLogType>>(emptyList())}
    var showActivityTypesModal by remember { mutableStateOf(false) }

    var showQrDialog by remember { mutableStateOf(false) }
    var qrContent by remember { mutableStateOf("") }

    val filteredLogs = remember(activityLogs, searchText) {
        if (searchText.isBlank()) {
            activityLogs
        } else {
            activityLogs.filter { log ->
                log.activityType.contains(searchText, ignoreCase = true) ||
                log.comment.contains(searchText, ignoreCase = true)
            }
        }
    }

    fun fetchActivityLogsAndPet() {
        coroutineScope.launch {
            activityLogs = activityLogRepository.getActivityLogsTableForPet(UUID.fromString(petId))
            pet = petRepository.getPet(UUID.fromString(petId))
            activityTypes = activityLogRepository.getActivityLogsTypeTableForPet(UUID.fromString(petId))
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

            // search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search activities...") },
                placeholder = { Text("e.g. Walk, Feeding, Vet") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )

            if (searchText.isNotEmpty()) {
                Text(
                    text = if (filteredLogs.isEmpty()) 
                        "No activities found matching \"$searchText\""
                    else 
                        "Found ${filteredLogs.size} of ${activityLogs.size} activities",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    onClick = { viewMode = LogViewMode.LIST },
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("List")
                        }
                    },
                    selected = viewMode == LogViewMode.LIST,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                FilterChip(
                    onClick = { viewMode = LogViewMode.CALENDAR },
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calendar")
                        }
                    },
                    selected = viewMode == LogViewMode.CALENDAR,
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    onClick = { viewMode = LogViewMode.CHART },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stats")
                        }
                    },
                    selected = viewMode == LogViewMode.CHART
                )
            }

            when (viewMode) {
                LogViewMode.LIST -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredLogs) { log ->
                            ActivityLogComponent(activityLog = log)
                        }
                    }
                }
                LogViewMode.CALENDAR -> {
                    ActivityLogCalendar(
                        activityLogs = filteredLogs,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                LogViewMode.CHART -> {
                    TrendChart(
                        activityLogs = filteredLogs,
                        modifier = Modifier
                            .padding(bottom = 48.dp)
                            .fillMaxSize()
                    )
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

        Button(
            onClick = { showActivityTypesModal = true },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text("View Activity Types")
        }

        if (showActivityTypesModal) {
            AlertDialog(
                onDismissRequest = { showActivityTypesModal = false },
                title = { Text("Activity Types") },
                text = {
                    Column {
                        activityTypes.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.activityType.uppercase(), fontSize = 16.sp)
                                Button(onClick = {
                                    val userId = userRepository.getCurrentUserId()
                                    val url = "https://cs446-project-production.up.railway.app"
                                    qrContent = "$url?petId=$petId&userId=$userId&type=${type.activityType}"
                                    showQrDialog = true
                                }) {
                                    Text("Create QR Code")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showActivityTypesModal = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showQrDialog) {
            AlertDialog(
                onDismissRequest = { showQrDialog = false },
                title = { Text("QR Code") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val qrBitmap = generateQrCodeBitmap(qrContent)
                        qrBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(200.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            qrBitmap?.let { saveQrToGallery(context, it) }
                        }) {
                            Text("Save QR Code")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showQrDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showActivityLogModal) {
            Dialog(
                onDismissRequest = {
                    showActivityLogModal = false
                    fetchActivityLogsAndPet()
                },
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
                            petId = pet!!.id,
                            activityLogRepository = activityLogRepository,
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

private fun generateQrCodeBitmap(content: String): Bitmap? {
    val size = 512
    return try {
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveQrToGallery(context: Context, bitmap: Bitmap, fileName: String = "qr_code_${System.currentTimeMillis()}") {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Toast.makeText(context, "QR code saved to gallery", Toast.LENGTH_SHORT).show()
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
