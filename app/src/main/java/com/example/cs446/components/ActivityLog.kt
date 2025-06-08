package com.example.cs446.components

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.ui.theme.CS446Theme
import java.time.Instant
import java.util.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.O) // For modern dates
@Composable
fun ActivityLogForm(
    modifier: Modifier = Modifier,
    onSubmit: (activityDate: Instant, activityType: String, comment: String, makePost: Boolean, imageUri: String?) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    var activityDate by remember { mutableStateOf(LocalDate.now()) }
    var activityType by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var makePost by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
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
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select an Image")
            }

            imageUri?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
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
                    imageUri?.toString()
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
            onSubmit = { _, _, _, _, _ -> }
        )
    }
}
