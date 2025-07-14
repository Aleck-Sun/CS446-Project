package com.example.cs446.ui.components.pets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTypeSelector(
    activityType: String,
    onActivityTypeChange: (String) -> Unit,
    activityTypeOptions: List<String>,
    onNewTypeConfirmed: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(activityType) }
    var hasUserTyped by remember { mutableStateOf(false) }

    LaunchedEffect(activityType) {
        if (inputText != activityType) {
            inputText = activityType
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
            if (!it) {
                hasUserTyped = false
            }
        },
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                hasUserTyped = true
            },
            label = { Text("Activity Type") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true
        )

        if (expanded) {
            ExposedDropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false }
            ) {
                val optionsToShow = if (hasUserTyped) {
                    activityTypeOptions.filter { it.contains(inputText, ignoreCase = true) }
                } else {
                    activityTypeOptions
                }

                optionsToShow.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            inputText = option
                            onActivityTypeChange(option)
                            expanded = false
                            hasUserTyped = false
                        }
                    )
                }

                val isExactMatch = activityTypeOptions.any { it.equals(inputText, ignoreCase = true) }
                if (inputText.isNotBlank() && !isExactMatch) {
                    DropdownMenuItem(
                        text = { Text("Add \"$inputText\"") },
                        onClick = {
                            val newType = inputText
                            onNewTypeConfirmed(newType)
                            onActivityTypeChange(newType)
                            expanded = false
                            hasUserTyped = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun ActivityTypeSelectorPreview() {
    val activityTypes = remember { mutableStateListOf("Walk", "Feeding", "Vet Visit") }
    var selectedType by remember { mutableStateOf("") }

    ActivityTypeSelector(
        activityType = selectedType,
        onActivityTypeChange = { selectedType = it },
        activityTypeOptions = activityTypes,
        onNewTypeConfirmed = {
            if (it !in activityTypes) {
                activityTypes.add(it)
            }
        }
    )
}

