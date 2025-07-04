package com.example.cs446.ui.pages.main.pets

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.atStartOfDayIn
import java.time.format.DateTimeFormatter

import com.example.cs446.backend.data.model.Breed
import com.example.cs446.backend.data.model.Species
import com.example.cs446.backend.data.model.speciesOfBreed
import com.example.cs446.ui.components.DropdownSelector
import com.example.cs446.ui.components.DatePickerTextField

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, species: Species, breed: Breed, birthdate: Instant, weight: Double, imageUri: Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf<Species?>(null) }
    var selectedBreed by remember { mutableStateOf<Breed?>(null) }
    var birthdate by remember { mutableStateOf(java.time.LocalDate.now()) }
    var weight by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var nameError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var speciesError by remember { mutableStateOf(false) }
    var breedError by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
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

                val birthdateInstant = kotlinx.datetime.LocalDate(
                    birthdate.year,
                    birthdate.monthValue,
                    birthdate.dayOfMonth
                ).atStartOfDayIn(TimeZone.currentSystemDefault())
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
                if (nameError) Text("Name cannot be empty.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Species
                DropdownSelector(
                    label = "Species",
                    selectedValue = selectedSpecies,
                    options = Species.entries.toList(),
                    onValueSelected = { newSpecies ->
                        // Reset breed if species has changed
                        if (newSpecies != selectedSpecies) {
                            selectedBreed = null
                        }
                        selectedSpecies = newSpecies
                        speciesError = false
                    },
                    isError = speciesError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (speciesError) Text("Please select a species.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Breed
                DropdownSelector(
                    label = "Breed",
                    selectedValue = selectedBreed,
                    options = selectedSpecies?.let { species ->
                        Breed.entries.filter { it == Breed.OTHER || speciesOfBreed(it) == species }
                    } ?: emptyList(),
                    onValueSelected = {
                        selectedBreed = it
                        breedError = false
                    },
                    isError = breedError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (breedError) Text("Please select a breed.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                DatePickerTextField(
                    date = birthdate,
                    label = "Date of Birth",
                    formatter = formatter,
                    onDateChange = { birthdate = it }
                )

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
