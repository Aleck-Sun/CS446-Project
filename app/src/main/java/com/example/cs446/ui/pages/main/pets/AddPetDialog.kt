package com.example.cs446.ui.pages.main.pets

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.backend.data.model.Breed
import com.example.cs446.backend.data.model.Species
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    // name, species, breed, birthdate, weight, imageUri
    onAdd: (String, Species, Breed, Instant, Double, Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf<Species?>(null) }
    var selectedBreed by remember { mutableStateOf<Breed?>(null) }
    var birthdate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var weight by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var speciesDropdownExpanded by remember { mutableStateOf(false) }
    var breedDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var speciesError by remember { mutableStateOf(false) }
    var breedError by remember { mutableStateOf(false) }

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
                speciesError = selectedSpecies == null
                breedError = selectedBreed == null

                val weightValue = weight.toDoubleOrNull()
                weightError = weightValue == null || weightValue <= 0.0

                val birthdateInstant = birthdate.atStartOfDayIn(TimeZone.currentSystemDefault())
                val birthdateError = birthdateInstant > Clock.System.now()

                if (!nameError && !speciesError && !breedError && !birthdateError && !weightError) {
                    onAdd(name, selectedSpecies!!, selectedBreed!!, birthdateInstant, weightValue!!, selectedImageUri)
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

                // Name
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
                Box {
                    OutlinedTextField(
                        value = selectedSpecies?.toString() ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Species") },
                        readOnly = true,
                        isError = speciesError
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { speciesDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = speciesDropdownExpanded,
                        onDismissRequest = { speciesDropdownExpanded = false }
                    ) {
                        Species.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(it.toString()) },
                                onClick = {
                                    selectedSpecies = it
                                    speciesError = false
                                    speciesDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (speciesError) Text("Please select a species.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Breed
                Box {
                    OutlinedTextField(
                        value = selectedBreed?.toString() ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Breed") },
                        readOnly = true,
                        isError = breedError
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { breedDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = breedDropdownExpanded,
                        onDismissRequest = { breedDropdownExpanded = false }
                    ) {
                        Breed.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(it.toString()) },
                                onClick = {
                                    selectedBreed = it
                                    breedError = false
                                    breedDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (breedError) Text("Please select a breed.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

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
