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
import com.example.cs446.backend.data.model.Permissions
import java.util.UUID

@Composable
fun HandlerCard(
    handler: Handler,
    onPermissionChange: (String, Boolean) -> Unit,
    currentUserId: UUID,
    canEditPermissions: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            val isSelf = handler.userId == currentUserId
            Text(
                text = if (isSelf) handler.name + " (You)" else handler.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = handler.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (handler.role != "Owner") {
                val canEdit = canEditPermissions && !isSelf
                val canEditSelf = false // never allow editing own permissions

                PermissionCheckbox(
                    label = "Edit statistics",
                    checked = handler.permissions.editStatistics,
                    onCheckedChange = { onPermissionChange("editStatistics", it) },
                    enabled = if (isSelf) canEditSelf else canEdit
                )
                PermissionCheckbox(
                    label = "Edit logs",
                    checked = handler.permissions.editLogs,
                    onCheckedChange = { onPermissionChange("editLogs", it) },
                    enabled = if (isSelf) canEditSelf else canEdit
                )
                PermissionCheckbox(
                    label = "Set reminders",
                    checked = handler.permissions.setReminders,
                    onCheckedChange = { onPermissionChange("setReminders", it) },
                    enabled = if (isSelf) canEditSelf else canEdit
                )
                PermissionCheckbox(
                    label = "Invite handlers",
                    checked = handler.permissions.inviteHandlers,
                    onCheckedChange = { onPermissionChange("inviteHandlers", it) },
                    enabled = if (isSelf) canEditSelf else canEdit
                )
                PermissionCheckbox(
                    label = "Make posts for the pet",
                    checked = handler.permissions.makePosts,
                    onCheckedChange = { onPermissionChange("makePosts", it) },
                    enabled = if (isSelf) canEditSelf else canEdit
                )
                PermissionCheckbox(
                    label = "Edit permissions of others",
                    checked = handler.permissions.editPermissionsOfOthers,
                    onCheckedChange = { onPermissionChange("editPermissionsOfOthers", it) },
                    enabled = if (isSelf) canEditSelf else canEdit
                )
            }
        }
    }
}

// For Dialog
@Composable
fun HandlerCard(
    permissions: Permissions,
    onPermissionChange: (String, Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

            PermissionCheckbox(
                label = "Edit statistics",
                checked = permissions.editStatistics,
                onCheckedChange = { onPermissionChange("editStatistics", it) },
                enabled = true
            )
            PermissionCheckbox(
                label = "Edit logs",
                checked = permissions.editLogs,
                onCheckedChange = { onPermissionChange("editLogs", it) },
                enabled = true
            )
            PermissionCheckbox(
                label = "Set reminders",
                checked = permissions.setReminders,
                onCheckedChange = { onPermissionChange("setReminders", it) },
                enabled = true
            )
            PermissionCheckbox(
                label = "Invite handlers",
                checked = permissions.inviteHandlers,
                onCheckedChange = { onPermissionChange("inviteHandlers", it) },
                enabled = true
            )
            PermissionCheckbox(
                label = "Make posts for the pet",
                checked = permissions.makePosts,
                onCheckedChange = { onPermissionChange("makePosts", it) },
                enabled = true
            )
            PermissionCheckbox(
                label = "Edit permissions of others",
                checked = permissions.editPermissionsOfOthers,
                onCheckedChange = { onPermissionChange("editPermissionsOfOthers", it) },
                enabled = true
            )
        }
    }
}