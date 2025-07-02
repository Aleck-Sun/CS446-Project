package com.example.cs446.ui.components.pets

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.ui.components.DatePickerTextField
import com.example.cs446.ui.theme.CS446Theme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O) // For modern dates
@Composable
fun ActivityLogForm(
    modifier: Modifier = Modifier,
    onSubmit: (activityDate: Instant, activityType: String, comment: String, makePost: Boolean, makePublic: Boolean, selectedImagesUri: List<Uri>) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    var activityDate by remember { mutableStateOf(LocalDate.now()) }
    var activityType by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var makePost by remember { mutableStateOf(false) }

    var currentImageIndex by remember { mutableStateOf<Int?>(null) }
    val selectedImagesUri = remember { mutableStateListOf<Uri>() }
    var makePublic by remember { mutableStateOf<Boolean>(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImagesUri.add(it)
        }
        currentImageIndex = selectedImagesUri.size-1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Log Pet Activity", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerTextField(
            date = activityDate,
            label = "Activity Date",
            formatter = formatter,
            onDateChange = {
                activityDate = it
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = activityType,
            onValueChange = { activityType = it },
            label = { Text("Activity Type") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Post to Pet Profile")
            Switch(
                checked = makePost,
                onCheckedChange = { makePost = it },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (makePost) {
            Text("Pet Photos", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp)
                ) {
                    items(selectedImagesUri.size + 1) {
                            index ->
                        if (index < selectedImagesUri.size)
                        {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImagesUri[index]),
                                contentDescription = "Selected pet image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (index == currentImageIndex) 3.dp else 0.dp,
                                        color = if (index == currentImageIndex) Color.Magenta else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { currentImageIndex = index },
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add photo",
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.Gray
                                    )
                                    Text("Add photo", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = makePublic,
                    onCheckedChange = { makePublic = it },
                    modifier = Modifier,
                    enabled = true,
                )
                Text("Make post public?")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSubmit(
                    activityDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
                    activityType,
                    comment,
                    makePost,
                    makePublic,
                    selectedImagesUri
                )
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Log Activity")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ActivityLogFormPreview() {
    CS446Theme {
        ActivityLogForm(
            onSubmit = { _, _, _, _, _, _ -> }
        )
    }
}
