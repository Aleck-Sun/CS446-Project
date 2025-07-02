package com.example.cs446.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs446.backend.data.model.Handler

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