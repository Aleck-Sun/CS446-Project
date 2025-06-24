package com.example.cs446.ui.pages.main.pets

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.model.Species
import com.example.cs446.backend.data.model.Breed
import com.example.cs446.backend.data.model.speciesOfBreed
import com.example.cs446.ui.components.DropdownSelector


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetDialog(
    pet: Pet,
    onDismiss: () -> Unit,
    onSave: (String, Species, Breed, Instant, Double, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(pet.name) }
    var selectedSpecies by remember { mutableStateOf(pet.species) }
    var selectedBreed by remember { mutableStateOf<Breed?>(pet.breed) }
    var birthdate by remember {
        mutableStateOf(
            pet.birthdate.toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
    }
    var weight by remember { mutableStateOf(pet.weight.toString()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var speciesError by remember { mutableStateOf(false) }
    var breedError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        birthdate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Validation
                nameError = name.isBlank()
                breedError = selectedBreed == null

                val weightValue = weight.toDoubleOrNull()
                weightError = weightValue == null || weightValue <= 0.0

                val birthdateInstant = birthdate.atStartOfDayIn(TimeZone.currentSystemDefault())
                val birthdateError = birthdateInstant > Clock.System.now()

                if (!nameError && !breedError && !birthdateError && !weightError) {
                    onSave(name, selectedSpecies, selectedBreed!!, birthdateInstant, weightValue!!, selectedImageUri)
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
                Text("Pet Photo", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected pet image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else if (pet.imageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(pet.imageUrl),
                                contentDescription = "Current pet image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
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
                                Text("Tap to add photo", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

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

                // Species
                DropdownSelector(
                    label = "Species",
                    selectedValue = selectedSpecies,
                    options = Species.entries.toList(),
                    onValueSelected = {
                        if (it != selectedSpecies) {
                            selectedSpecies = it
                            selectedBreed = null // Only reset if species changes
                        }
                        speciesError = false
                    },
                    isError = speciesError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (speciesError) Text("Please select a species.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Birthdate
                OutlinedTextField(
                    value = birthdate.toString(),
                    onValueChange = { },
                    label = { Text("Date of Birth") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = true,
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select date",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                // Breed
                DropdownSelector(
                    label = "Breed",
                    selectedValue = selectedBreed,
                    options = selectedSpecies.let { species ->
                        Breed.entries.filter { it == Breed.OTHER || speciesOfBreed(it) == species }
                    },
                    onValueSelected = {
                        selectedBreed = it
                        breedError = false
                    },
                    isError = breedError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (breedError) Text("Please select a breed.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                // Weight
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