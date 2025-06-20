package com.example.cs446.ui.pages.main.pets

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.ImageRepository
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.ui.pages.main.formatDate
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import java.time.LocalDate
import java.time.LocalDate.ofEpochDay
import java.time.Period
import java.util.UUID

@Composable
fun PetsScreen(
    onNavigate: (MainActivityDestination, String?) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val imageRepository = remember { ImageRepository() }
    val petRepository = remember { PetRepository() }
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showRemovePetDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // TODO: implement backend
    var pets by remember {
        mutableStateOf(
            listOf(
                Pet(
                    UUID.randomUUID(),
                    Instant.parse("2021-02-03T00:00:00Z"),
                    "Charlie",
                    "Dog", "Golden Retriever",
                    UUID.randomUUID(),
                    Instant.parse("2025-05-28T00:00:00Z"),
                    65.0
                ),
                Pet(
                    UUID.randomUUID(),
                    Instant.parse("2024-11-06T00:00:00Z"),
                    "Colin",
                    "Dog", "Beagle",
                    UUID.randomUUID(),
                    Instant.parse("2024-01-15T00:00:00Z"),
                    40.0
                ),
                Pet(
                    UUID.randomUUID(),
                    Instant.parse("2025-02-08T00:00:00Z"),
                    "Robin",
                    "Dog",
                    "Poodle",
                    UUID.randomUUID(),
                    Instant.parse("2023-11-02T00:00:00Z"),
                    30.0
                )
            )
        )
    }

    var selectedPetId by remember { mutableStateOf(pets.firstOrNull()?.id) }
    val selectedPet = selectedPetId?.let { id -> pets.find { it.id == id } }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            errorMessage = null
        }
    }

    @SuppressLint("NewApi") // TODO this gets rid of the compiler errors idk why
    fun calculateAge(birthdate: Instant): String {
        val birthLocalDate = ofEpochDay(birthdate.toEpochMilliseconds() / (24 * 60 * 60 * 1000))
        val today = LocalDate.now()
        val period = Period.between(birthLocalDate, today)
        return when {
            period.years > 0 -> "${period.years} year${if (period.years > 1) "s" else ""}"
            period.months > 0 -> "${period.months} month${if (period.months > 1) "s" else ""}"
            else -> "${period.days} day${if (period.days > 1) "s" else ""}"
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Top icons (pets + add pet)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                pets.forEach { pet ->
                    Column(
                        modifier = Modifier.clickable { selectedPetId = pet.id },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (pet.imageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(pet.imageUrl),
                                contentDescription = pet.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Face,
                                contentDescription = pet.name,
                                modifier = Modifier.size(56.dp),
                                tint = if (pet.id == selectedPetId) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                        Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        showAddPetDialog = true
                    }
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Pet", modifier = Modifier.size(56.dp))
                    Text("Add Pet", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pet info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (selectedPet != null) {
                    // Display pet info
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedPet.imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedPet.imageUrl),
                                    contentDescription = selectedPet.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Text(selectedPet.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val birthdateString = formatDate(selectedPet.birthdate)

                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Breed", fontWeight = FontWeight.Medium)
                                Text(selectedPet.breed ?: "Unknown")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date of Birth", fontWeight = FontWeight.Medium)
                                Text("${formatDate(selectedPet.birthdate)} (${calculateAge(selectedPet.birthdate)} old)")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sex", fontWeight = FontWeight.Medium)
                                Text("Unknown")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Weight", fontWeight = FontWeight.Medium)
                                Text("${selectedPet.weight} lbs")
                            }
                        }
                    }
                    // Edit/Remove current pet
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showEditPetDialog = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            onClick = { showRemovePetDialog = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logs button
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onNavigate(MainActivityDestination.Logs, selectedPetId.toString()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logs")
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .background(Color.Red, shape = CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("8", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Family button
            Button(
                onClick = { onNavigate(MainActivityDestination.Family, null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Family")
            }
        }
    }

    if (showAddPetDialog) {
        AddPetDialog(
            onDismiss = { showAddPetDialog = false },
            onAdd = { name, breed, weightStr, birthdate, imageUri ->
                // TODO: get pet UUID from database insert rather than defining it on the client side
                val petId = UUID.randomUUID()
                val newPet = Pet(
                    id = petId,
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    name = name,
                    species = "Dog",
                    breed = breed,
                    createdBy = UUID.randomUUID(),
                    birthdate = birthdate,
                    weight = weightStr.toDoubleOrNull() ?: 0.0,
                    imageUrl = null
                )
                
                pets = pets.toMutableList().apply { add(newPet) }
                selectedPetId = newPet.id
                
                // if image was selected, upload it
                if (imageUri != null) {
                    coroutineScope.launch {
                        try {
                            val imageUrl = imageRepository.uploadPetImage(context, imageUri, petId)
                            if (imageUrl != null) {
                                // update pet with image URL
                                val updatedPet = newPet.copy(imageUrl = imageUrl)
                                val updatedList = pets.toMutableList()
                                val index = updatedList.indexOfFirst { it.id == petId }
                                if (index != -1) {
                                    updatedList[index] = updatedPet
                                    pets = updatedList
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorMessage = "Failed to upload pet image. Please try again."
                        }
                    }
                }
                
                showAddPetDialog = false
            }
        )
    }

    if (showEditPetDialog && selectedPet != null) {
        EditPetDialog(
            pet = selectedPet,
            onDismiss = { showEditPetDialog = false },
            onSave = { newName, newBreed, newWeightStr, newBirthdate, imageUri ->
                val updatedList = pets.toMutableList()
                val updatedPet = selectedPet.copy(
                    name = newName,
                    breed = newBreed,
                    weight = newWeightStr.toDoubleOrNull() ?: selectedPet.weight,
                    birthdate = newBirthdate
                )
                val index = updatedList.indexOfFirst { it.id == selectedPetId }
                if (index != -1) {
                    updatedList[index] = updatedPet
                    pets = updatedList
                }
                
                // if new image was selected, upload it
                if (imageUri != null) {
                    coroutineScope.launch {
                        try {
                            val imageUrl = imageRepository.uploadPetImage(context, imageUri, selectedPet.id)
                            if (imageUrl != null) {
                                // update pet with new image URL
                                val finalUpdatedPet = updatedPet.copy(imageUrl = imageUrl)
                                val finalUpdatedList = pets.toMutableList()
                                val finalIndex = finalUpdatedList.indexOfFirst { it.id == selectedPetId }
                                if (finalIndex != -1) {
                                    finalUpdatedList[finalIndex] = finalUpdatedPet
                                    pets = finalUpdatedList
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorMessage = "Failed to update pet image. Please try again."
                        }
                    }
                }
                
                showEditPetDialog = false
            }
        )
    }

    if (showRemovePetDialog && selectedPet != null) {
        RemovePetDialog(
            petName = selectedPet.name,
            onConfirm = {
                val removeIndex = pets.indexOfFirst { it.id == selectedPetId }
                if (removeIndex != -1) {
                    val updatedList = pets.toMutableList()
                    updatedList.removeAt(removeIndex)
                    pets = updatedList

                    selectedPetId = when {
                        updatedList.isEmpty() -> null
                        removeIndex >= updatedList.size -> updatedList.last().id
                        else -> updatedList[removeIndex].id
                    }
                    showRemovePetDialog = false
                }
            },
            onDismiss = {
                showRemovePetDialog = false
            }
        )
    }
}
