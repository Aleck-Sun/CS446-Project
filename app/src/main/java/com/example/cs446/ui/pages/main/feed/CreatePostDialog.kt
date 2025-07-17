package com.example.cs446.ui.pages.main.feed

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

@Composable
fun CreatePostDialog(
    pets: List<Pet> = emptyList<Pet>(),
    onDismiss: () -> Unit = {},
    onPost: (Context, String, UUID, List<Uri>, Boolean) -> Unit = {_, _, _, _, _ ->  },
    postResult: PostResult = PostResult.Idling,
    sharedText: String = "",
    sharedImageUris: List<Uri> = emptyList<Uri>()
) {
    var currentImageIndex by remember { mutableStateOf<Int?>(null) }
    val selectedImagesUri = remember { mutableStateListOf<Uri>() }
    var text by remember { mutableStateOf<String>(sharedText) }
    var selectedPet by remember { mutableStateOf<Pet?>(if (pets.isEmpty()) null else pets[0]) }
    var makePublic by remember { mutableStateOf<Boolean>(false) }

    sharedImageUris.forEach {
        if (it !in selectedImagesUri) {
            selectedImagesUri.add(it)
        }
    }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImagesUri.add(it)
        }
        currentImageIndex = selectedImagesUri.size-1
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (postResult is PostResult.Posting) {
                    return@TextButton
                }
                selectedPet?.let {
                    onPost(context, text, it.id, selectedImagesUri, makePublic)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (postResult is PostResult.Posting) {
                    return@TextButton
                }
                onDismiss()
            }) {
                Text("Cancel")
            }
        },
        title = { Text("Create New Post") },
        text = {
            Column {
                if (pets.isEmpty()) {
                    Text(
                        "You do not have any pets.",
                        color = Color.Red
                    )
                } else {
                    if (selectedPet == null) {
                        selectedPet = pets.first()
                    }
                    DropdownPetSelector(
                        pets,
                        selectedPet!!,
                    ) { pet -> selectedPet = pet}
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text("Pet Photo", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
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
                                            color = Color.Transparent,
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    label = { Text("Add a caption...") },
                    modifier = Modifier.fillMaxWidth()
                )

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

                if (makePublic) {
                    Text(
                        text = "Having a public post on your profile will make your pet profile public!",
                        color = Color(0xFFDEBC11)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                when (postResult) {
                    is PostResult.Posting -> Text(
                        text = "Please wait...",
                        color = Color.Green
                    )
                    is PostResult.PostError -> Text(
                        text = "Error: ${postResult.message}",
                        color = Color.Red
                    )
                    else -> Spacer(Modifier)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CreatePostDialogPreview() {
    CreatePostDialog()
}
