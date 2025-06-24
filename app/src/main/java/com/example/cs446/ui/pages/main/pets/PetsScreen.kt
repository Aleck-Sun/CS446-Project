package com.example.cs446.ui.pages.main.pets

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
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.ImageRepository
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.ui.pages.main.formatDate
import com.example.cs446.ui.pages.main.calculateAge
import com.example.cs446.backend.data.model.UserPetRelation
import com.example.cs446.backend.data.model.Permissions
import com.example.cs446.backend.data.repository.petToRaw
import kotlinx.datetime.Clock

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

    var currentUserId by remember { mutableStateOf<UUID?>(null) }
    var pets by remember { mutableStateOf(emptyList<Pet>()) }
    var selectedPetId by remember { mutableStateOf(pets.firstOrNull()?.id) }
    val selectedPet = selectedPetId?.let { id -> pets.find { it.id == id } }

    // Load pets the users created from backend. TODO: change to pets that have any relation with user
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                currentUserId = UserRepository().getCurrentUserId()
                    ?: throw IllegalStateException("User ID is null")
                pets = petRepository.getPetsRelatedToUser(currentUserId!!)
                selectedPetId = pets.firstOrNull()?.id
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Failed to initialize user and pets."
            }
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            errorMessage = null
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
                    // Display pet info (Breed, Weight, Date of Birth, Age)
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

                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Breed", fontWeight = FontWeight.Medium)
                                Text(selectedPet.breed.toString())
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Weight", fontWeight = FontWeight.Medium)
                                Text("%.2f lbs".format(selectedPet.weight))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date of Birth", fontWeight = FontWeight.Medium)
                                Text(formatDate(selectedPet.birthdate))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Age", fontWeight = FontWeight.Medium)
                                Text("${calculateAge(selectedPet.birthdate)} Old")
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
            onAdd = { name, species, breed, birthdate, weight, imageUri ->
                coroutineScope.launch {
                    try {
                        // TODO: get pet UUID from database insert rather than defining it on the client side
                        val petId = UUID.randomUUID()
                        var imageUrl: String? = null
                        if (imageUri != null) {
                            imageUrl = imageRepository.uploadPetImage(context, imageUri, petId)
                            if (imageUrl == null) {
                                errorMessage = "Failed to upload image."
                            }
                        }
                        val newPet = Pet(
                            id = petId,
                            createdAt = Clock.System.now(),
                            name = name,
                            species = species,
                            breed = breed,
                            createdBy = currentUserId!!,
                            birthdate = birthdate,
                            weight = weight,
                            imageUrl = imageUrl
                        )
                        petRepository.addPet(newPet)
                        petRepository.addUserPetRelation(
                            UserPetRelation(
                                userId = currentUserId!!,
                                petId = petId,
                                relation = "Owner",
                                permissions = Permissions(
                                    editLogs = true,
                                    setReminders = true,
                                    inviteHandlers = true,
                                    makePosts = true,
                                    editPermissionsOfOthers = true
                                )
                            )
                        )
                        pets = petRepository.getPetsRelatedToUser(currentUserId!!)
                        selectedPetId = petId
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Failed to add pet."
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
            onSave = { newName, newSpecies, newBreed, newBirthdate, newWeight, newImageUri ->
                coroutineScope.launch {
                    try {
                        val newImageUrl: String? = if (newImageUri != null) {
                            val url = imageRepository.uploadPetImage(context, newImageUri, selectedPet.id)
                            if (url == null) {
                                errorMessage = "Failed to upload image."
                            }
                            url
                        } else {
                            selectedPet.imageUrl
                        }

                        val updatedPet = selectedPet.copy(
                            name = newName,
                            species = newSpecies,
                            breed = newBreed,
                            birthdate = newBirthdate,
                            weight = newWeight,
                            imageUrl = newImageUrl
                        )
                        petRepository.updatePet(updatedPet)
                        pets = petRepository.getPetsRelatedToUser(currentUserId!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Failed to update pet."
                    }
                    showEditPetDialog = false
                }
            }
        )
    }

    if (showRemovePetDialog && selectedPet != null) {
        RemovePetDialog(
            petName = selectedPet.name,
            onConfirm = {
                coroutineScope.launch {
                    try {
                        petRepository.deletePet(selectedPet.id)
                        pets = petRepository.getPetsRelatedToUser(currentUserId!!)
                        selectedPetId = pets.firstOrNull()?.id
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Failed to remove pet."
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