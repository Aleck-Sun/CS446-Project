package com.example.cs446.ui.pages.main.pets

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cs446.ui.components.ActivityLogForm
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.ui.theme.CS446Theme
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    onNavigate: (MainActivityDestination) -> Unit
) {
    var showActivityLogModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
            // TODO (BOWEN): Add pet name in front
            Text(
                text = "Activity Logs",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TODO (BOWEN): Add existing logs list here
            // LazyColumn { ... }
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
                                handleActivitySubmission(
                                    activityDate = activityDate,
                                    activityType = activityType,
                                    comment = comment,
                                    makePost = makePost,
                                    imageUri = imageUri
                                )
                                showActivityLogModal = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun handleActivitySubmission(
    activityDate: Instant,
    activityType: String,
    comment: String,
    makePost: Boolean,
    imageUri: Uri?
) {
    if (makePost) {
        // TODO: Implement posting logic
    }
    // TODO: Implement submission logic
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun LogsScreenPreview() {
    CS446Theme {
        LogsScreen(
            onNavigate = { }
        )
    }
}
