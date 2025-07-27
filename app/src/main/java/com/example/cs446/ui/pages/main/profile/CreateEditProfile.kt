package com.example.cs446.ui.pages.main.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.result.AuthResult
import com.example.cs446.backend.data.result.PostResult
import com.example.cs446.ui.components.feed.DropdownPetSelector
import kotlinx.coroutines.selects.select
import java.util.UUID
import kotlin.String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProfile(
    onDismiss: () -> Unit = {},
    onSave: (Context, Uri, String, String) -> Unit = {_, _, _, _ -> },
    usernameDefault: String = "",
    bioDefault: String = "",
    avatarDefault: Uri = Uri.EMPTY
) {
    var avatarImage by remember { mutableStateOf<Uri>(avatarDefault) }
    var usernameText by remember { mutableStateOf<String>(usernameDefault) }
    var bioText by remember { mutableStateOf<String>(bioDefault) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri: Uri? ->
        uri?.let {
            avatarImage = it
        }
    }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(context, avatarImage, usernameText, bioText)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Profile") },
        text = {
            Column {
                Image(
                    painter = rememberAsyncImagePainter(avatarImage),
                    contentDescription = "Profile Avatar",
                    modifier = Modifier
                        .size(
                            if (avatarImage == Uri.EMPTY) { 0.dp }
                            else  { 100.dp }
                        )
                        .clickable { avatarPickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )

                Icon(
                    Icons.Default.Add,
                    contentDescription = "Upload Avatar",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Gray
                )
                Text("Upload Avatar", color = Color.Gray, fontSize = 12.sp)

                OutlinedTextField(
                    value = usernameText,
                    onValueChange = {
                        usernameText = it
                    },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bioText,
                    onValueChange = {
                        bioText = it
                    },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CreatePostDialogPreview() {
    CreateEditProfile()
}
