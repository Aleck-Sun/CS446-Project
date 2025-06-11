package com.example.cs446.ui.pages.main.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.cs446.data.model.Pet
import com.example.cs446.ui.pages.main.MainActivityDestination

@Composable
fun PetsScreen(
    onNavigate: (MainActivityDestination) -> Unit
) {
    val context = LocalContext.current

    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showRemovePetDialog by remember { mutableStateOf(false) }

    // TODO: implement backend
    var pets by remember {
        mutableStateOf(
            listOf(
                Pet(UUID.randomUUID(), Instant.parse("2021-02-03T00:00:00Z"), "Charlie", 1, "Golden Retriever", UUID.randomUUID(), Instant.parse("2025-05-28T00:00:00Z"), 65.0),
                Pet(UUID.randomUUID(), Instant.parse("2024-11-06T00:00:00Z"), "Colin", 1, "Beagle", UUID.randomUUID(), Instant.parse("2024-01-15T00:00:00Z"), 40.0),
                Pet(UUID.randomUUID(), Instant.parse("2025-02-08T00:00:00Z"), "Robin", 1, "Poodle", UUID.randomUUID(), Instant.parse("2023-11-02T00:00:00Z"), 30.0)
            )
        )
    }

    var selectedPetId by remember { mutableStateOf(pets.firstOrNull()?.id) }
    val selectedPet = selectedPetId?.let { id -> pets.find { it.id == id } }

    Scaffold() { innerPadding ->
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
                        Icon(
                            Icons.Default.Face,
                            contentDescription = pet.name,
                            modifier = Modifier.size(56.dp),
                            tint = if (pet.id == selectedPetId) MaterialTheme.colorScheme.primary else Color.Gray
                        )
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
                        Text(selectedPet.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        val birthdateString = formatDate(selectedPet.birthdate)

                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Breed", fontWeight = FontWeight.Medium)
                                Text(selectedPet.breed ?: "Unknown")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date of Birth", fontWeight = FontWeight.Medium)
                                Text(birthdateString)
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
                    onClick = { onNavigate(MainActivityDestination.Logs) },
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
                onClick = { onNavigate(MainActivityDestination.Family) },
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
            onAdd = { name, breed, weightStr ->
                val newPet = Pet(
                    id = UUID.randomUUID(),
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    name = name,
                    species = 1,
                    breed = breed,
                    creatorId = UUID.randomUUID(),
                    birthdate = Instant.parse("2024-01-01T00:00:00Z"),
                    weight = weightStr.toDoubleOrNull() ?: 0.0
                )
                pets = pets.toMutableList().apply { add(newPet) }
                selectedPetId = newPet.id
                showAddPetDialog = false
            }
        )
    }

    if (showEditPetDialog && selectedPet != null) {
        EditPetDialog(
            pet = selectedPet,
            onDismiss = { showEditPetDialog = false },
            onSave = { newName, newBreed, newWeightStr ->
                val updatedList = pets.toMutableList()
                val updatedPet = selectedPet.copy(
                    name = newName,
                    breed = newBreed,
                    weight = newWeightStr.toDoubleOrNull() ?: selectedPet.weight
                )
                val index = updatedList.indexOfFirst { it.id == selectedPetId }
                if (index != -1) {
                    updatedList[index] = updatedPet
                    pets = updatedList
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

@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Validation logic
                nameError = name.isBlank()
                val weightValue = weight.toDoubleOrNull()
                weightError = weightValue == null || weightValue <= 0.0

                if (!nameError && !weightError) {
                    onAdd(name, breed, weight)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add New Pet") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Pet Name") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) {
                    Text(
                        "Name cannot be empty.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                        weightError = false
                    },
                    label = { Text("Weight (lbs)") },
                    isError = weightError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (weightError) {
                    Text(
                        "Weight must be a positive number.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}


@Composable
fun EditPetDialog(
    pet: Pet,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(pet.name) }
    var breed by remember { mutableStateOf(pet.breed ?: "") }
    var weight by remember { mutableStateOf(pet.weight.toString()) }

    var nameError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Validation
                nameError = name.isBlank()
                weightError = weight.toDoubleOrNull() == null || weight.toDoubleOrNull()!! <= 0.0

                if (!nameError && !weightError) {
                    onSave(name, breed, weight)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Pet") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Pet Name") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) {
                    Text(
                        "Name cannot be empty.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                        weightError = false
                    },
                    label = { Text("Weight (lbs)") },
                    isError = weightError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (weightError) {
                    Text(
                        "Weight must be a positive number.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}

@Composable
fun RemovePetDialog(
    petName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Pet") },
        text = { Text("Are you sure you want to remove $petName? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatDate(instant: Instant): String {
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val millis = instant.toEpochMilliseconds()
    val date = Date(millis)
    return formatter.format(date)
}