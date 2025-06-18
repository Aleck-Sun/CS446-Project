package com.example.cs446.view.permissions

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs446.backend.data.model.Handler
import com.example.cs446.backend.data.model.Permissions
import java.util.UUID

class HandlerViewModel : ViewModel() {
    private val _handlers = mutableStateOf(
        listOf(
            Handler(
                id = UUID.randomUUID(),
                name = "Candice",
                role = "Owner",
                permissions = Permissions(
                    editLogs = true,
                    setReminders = true,
                    inviteHandlers = true,
                    makePosts = true,
                    editPermissionsOfOthers = true
                )
            ),
            Handler(
                id = UUID.randomUUID(),
                name = "John Doe",
                role = "Family member",
                permissions = Permissions(
                    editLogs = true,
                    setReminders = true,
                    inviteHandlers = true,
                    makePosts = true
                )
            ),
            Handler(
                id = UUID.randomUUID(),
                name = "Jane Doe",
                role = "Caretaker",
                permissions = Permissions(
                    editLogs = true,
                    setReminders = true,
                    inviteHandlers = true
                )
            )
        )
    )

    val handlers: List<Handler> by _handlers

    fun updatePermission(handlerName: String, permission: String, value: Boolean) {
        _handlers.value = _handlers.value.map { handler ->
            if (handler.name == handlerName) {
                handler.copy(
                    permissions = when (permission) {
                        "editLogs" -> handler.permissions.copy(editLogs = value)
                        "setReminders" -> handler.permissions.copy(setReminders = value)
                        "inviteHandlers" -> handler.permissions.copy(inviteHandlers = value)
                        "makePosts" -> handler.permissions.copy(makePosts = value)
                        "editPermissionsOfOthers" -> handler.permissions.copy(editPermissionsOfOthers = value)
                        else -> handler.permissions
                    }
                )
            } else {
                handler
            }
        }
    }
}

@Composable
fun HandlerPermissionsScreen(viewModel: HandlerViewModel = viewModel()) {
    // TODO: Based on the database value
    val canAddHandlers = true

    Scaffold(
        bottomBar = {
            if (canAddHandlers) {
                Button(
                    onClick = {
                        // TODO: Implement action for adding a new handler
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Add handler")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Charlie's Family",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(viewModel.handlers) { handler ->
                    HandlerCard(handler = handler, onPermissionChange = { perm, value ->
                        viewModel.updatePermission(handler.name, perm, value)
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HandlerCard(
    handler: Handler,
    onPermissionChange: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = handler.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = handler.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (handler.role != "Owner") {

                // Permissions
                PermissionCheckbox(
                    label = "Edit logs",
                    checked = handler.permissions.editLogs,
                    onCheckedChange = { onPermissionChange("editLogs", it) }
                )
                PermissionCheckbox(
                    label = "Set reminders",
                    checked = handler.permissions.setReminders,
                    onCheckedChange = { onPermissionChange("setReminders", it) }
                )
                PermissionCheckbox(
                    label = "Invite handlers",
                    checked = handler.permissions.inviteHandlers,
                    onCheckedChange = { onPermissionChange("inviteHandlers", it) }
                )
                PermissionCheckbox(
                    label = "Make posts for the pet",
                    checked = handler.permissions.makePosts,
                    onCheckedChange = { onPermissionChange("makePosts", it) }
                )
                PermissionCheckbox(
                    label = "Edit permissions of others",
                    checked = handler.permissions.editPermissionsOfOthers,
                    onCheckedChange = { onPermissionChange("editPermissionsOfOthers", it) }
                )

            }
        }
    }
}

@Composable
fun PermissionCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Text(
            text = label
        )
    }
}