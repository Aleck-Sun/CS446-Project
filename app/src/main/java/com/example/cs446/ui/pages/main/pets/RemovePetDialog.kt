package com.example.cs446.ui.pages.main.pets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun RemovePetDialog(
    action: String,
    petName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$action Pet") },
        text = { Text("Are you sure you want to ${action.lowercase()} $petName? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(action, color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}