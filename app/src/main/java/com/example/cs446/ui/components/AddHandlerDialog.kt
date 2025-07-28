package com.example.cs446.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs446.backend.data.model.Permissions

@Composable
fun AddHandlerDialog(
    onDismiss: () -> Unit,
    onInvite: (String, String, Permissions) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var relationName by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var permissions by remember { mutableStateOf(Permissions(editStatistics = true, editLogs = true, setReminders = true, makePosts = true)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (usernameOrEmail.isBlank()) {
                    localError = "Please enter a username or email"
                } else if (relationName.isBlank()) {
                    localError = "Please enter a relation name"
                } else if (relationName.trim().lowercase() == "owner"){
                    localError = "Relation name cannot be 'Owner'"
                } else {
                    localError = null
                    onInvite(
                        usernameOrEmail,
                        relationName,
                        permissions
                    )
                }
            }, enabled = !isLoading) {
                Text("Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Invite Handler") },
        text = {
            Column {
                OutlinedTextField(
                    value = usernameOrEmail,
                    onValueChange = { usernameOrEmail = it },
                    label = { Text("Username or Email") },
                    singleLine = true,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = relationName,
                    onValueChange = { relationName = it },
                    label = { Text("Relation") },
                    singleLine = true,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))
                HandlerCard(
                    permissions = permissions,
                    onPermissionChange = { perm, value ->
                        permissions = when (perm) {
                            "editStatistics" -> permissions.copy(editStatistics = value)
                            "editLogs" -> permissions.copy(editLogs = value)
                            "setReminders" -> permissions.copy(setReminders = value)
                            "inviteHandlers" -> permissions.copy(inviteHandlers = value)
                            "makePosts" -> permissions.copy(makePosts = value)
                            "editPermissionsOfOthers" -> permissions.copy(editPermissionsOfOthers = value)
                            else -> permissions
                        }
                    }
                )
                if (localError != null) Text(localError!!, color = MaterialTheme.colorScheme.error)
                if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}