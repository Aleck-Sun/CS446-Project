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
import com.example.cs446.ui.components.DropdownSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownPetSelector(
    petList: List<Pet>,
    selectedPet: Pet,
    onPetSelected: (Pet) -> Unit,
) {
    DropdownSelector<Pet>(
        label = "Select Pet",
        selectedValue = selectedPet,
        options = petList,
        onValueSelected = onPetSelected,
        toStringFunc = {
            it.name
        }
    )
}