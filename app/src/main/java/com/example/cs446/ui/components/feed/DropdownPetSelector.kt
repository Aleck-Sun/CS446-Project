package com.example.cs446.ui.components.feed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cs446.backend.data.model.Pet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownPetSelector(
    petList: List<Pet>,           // e.g. list of pet names or pet objects
    onPetSelected: (Int) -> Unit
) {
    var selectedPetIndex = 0
    var expanded by remember { mutableStateOf(false) }
    var selectedPetName by remember { mutableStateOf(petList[selectedPetIndex].name) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
            println(expanded)
        }
    ) {
        // Display selected pet's name
        OutlinedTextField(
            readOnly = true,
            value = selectedPetName,
            onValueChange = {},
            label = { Text("Select Pet") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            petList.forEachIndexed { i, pet ->
                DropdownMenuItem(
                    text = { Text(pet.name) },
                    onClick = {
                        onPetSelected(i)
                        expanded = false
                        selectedPetIndex = i
                        selectedPetName = petList[selectedPetIndex].name
                    }
                )
            }
        }
    }
}