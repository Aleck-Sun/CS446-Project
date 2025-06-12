package com.example.cs446.ui.pages.main.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs446.data.model.FamilyMember
import com.example.cs446.data.model.Permissions

class FamilyViewModel : ViewModel() {
    private val _familyMembers = mutableStateOf(
        listOf(
            FamilyMember(
                name = "Candice",
                role = "Owner",
                permissions = Permissions(
                    editLogs = true,
                    setReminders = true,
                    inviteMembers = true,
                    makePosts = true,
                    editPermissionsOfOthers = true
                )
            ),
            FamilyMember(
                name = "John Doe",
                role = "Family member",
                permissions = Permissions(
                    editLogs = true,
                    setReminders = true,
                    inviteMembers = true,
                    makePosts = true
                )
            ),
            FamilyMember(
                name = "Jane Doe",
                role = "Caretaker",
                permissions = Permissions(
                    editLogs = true,
                    setReminders = true,
                    inviteMembers = true
                )
            )
        )
    )

    val familyMembers: List<FamilyMember> by _familyMembers

    fun updatePermission(memberName: String, permission: String, value: Boolean) {
        _familyMembers.value = _familyMembers.value.map { member ->
            if (member.name == memberName) {
                member.copy(
                    permissions = when (permission) {
                        "editLogs" -> member.permissions.copy(editLogs = value)
                        "setReminders" -> member.permissions.copy(setReminders = value)
                        "inviteMembers" -> member.permissions.copy(inviteMembers = value)
                        "makePosts" -> member.permissions.copy(makePosts = value)
                        "editPermissionsOfOthers" -> member.permissions.copy(editPermissionsOfOthers = value)
                        else -> member.permissions
                    }
                )
            } else {
                member
            }
        }
    }
}

@Composable
fun FamilyPermissionsScreen(viewModel: FamilyViewModel = viewModel()) {
    // Based on the database value
    val canAddMembers = true

    Scaffold(
        bottomBar = {
            if (canAddMembers) {
                Button(
                    onClick = {
                        // TODO: Implement action for adding a new member
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Add member")
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
                items(viewModel.familyMembers) { member ->
                    FamilyMemberCard(member = member, onPermissionChange = { perm, value ->
                        viewModel.updatePermission(member.name, perm, value)
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FamilyMemberCard(
    member: FamilyMember,
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
                text = member.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = member.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (member.role != "Owner") {

                // Permissions
                PermissionCheckbox(
                    label = "Edit logs",
                    checked = member.permissions.editLogs,
                    onCheckedChange = { onPermissionChange("editLogs", it) }
                )
                PermissionCheckbox(
                    label = "Set reminders",
                    checked = member.permissions.setReminders,
                    onCheckedChange = { onPermissionChange("setReminders", it) }
                )
                PermissionCheckbox(
                    label = "Invite members",
                    checked = member.permissions.inviteMembers,
                    onCheckedChange = { onPermissionChange("inviteMembers", it) }
                )
                PermissionCheckbox(
                    label = "Make posts for the pet",
                    checked = member.permissions.makePosts,
                    onCheckedChange = { onPermissionChange("makePosts", it) }
                )
                PermissionCheckbox(
                    label = "Edit permissions of others",
                    checked = member.permissions.editPermissionsOfOthers,
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