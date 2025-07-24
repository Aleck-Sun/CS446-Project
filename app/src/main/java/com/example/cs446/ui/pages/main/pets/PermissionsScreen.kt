package com.example.cs446.ui.pages.main.pets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.ui.components.AddHandlerDialog
import com.example.cs446.ui.components.HandlerCard
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.view.pets.PermissionsViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun PermissionsScreen(
    petId: String,
    viewModel: PermissionsViewModel,
    onNavigate: (MainActivityDestination, String?) -> Unit
) {
    val petRepository = remember { PetRepository() }
    val userRepository = remember { UserRepository() }

    var pet by remember { mutableStateOf<Pet?>(null) }
    val handlers by viewModel.handlers.collectAsState()
    var loggedInUserId by remember { mutableStateOf<UUID?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val currentHandler = handlers.find { it.userId == loggedInUserId }
    val canAddHandlers by remember(currentHandler) {
        derivedStateOf { currentHandler?.permissions?.inviteHandlers ?: false }
    }
    val canEditPermissions by remember(currentHandler) {
        derivedStateOf { currentHandler?.permissions?.editPermissionsOfOthers == true }
    }

    var showAddHandlerDialog by remember { mutableStateOf(false) }
    var inviteError by remember { mutableStateOf<String?>(null) }
    var isInviting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(petId) {
        isLoading = true
        val startTime = System.currentTimeMillis()

        pet = petRepository.getPet(UUID.fromString(petId))
        loggedInUserId = userRepository.getCurrentUserId()
        viewModel.loadHandlers(UUID.fromString(petId))

        val elapsed = System.currentTimeMillis() - startTime
        val minLoadingTime = 2000L // ms
        if (elapsed < minLoadingTime) {
            delay(minLoadingTime - elapsed)
        }
        isLoading = false
    }

    if (isLoading || pet == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            bottomBar = {
                if (canAddHandlers) {
                    Button(
                        onClick = {
                            showAddHandlerDialog = true
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${pet?.name ?: "My Pet"}'s Family",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(
                        onClick = { onNavigate(MainActivityDestination.Pets, null) },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(handlers) { handler ->
                        HandlerCard(
                            handler = handler,
                            onPermissionChange = { perm, value ->
                                viewModel.updatePermission(
                                    handler.name,
                                    perm,
                                    value,
                                    UUID.fromString(petId)
                                )
                            },
                            currentUserId = loggedInUserId
                                ?: handler.userId, // fallback disables all if not loaded
                            canEditPermissions = canEditPermissions
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        if (showAddHandlerDialog) {
            AddHandlerDialog(
                onDismiss = {
                    showAddHandlerDialog = false
                    inviteError = null
                },
                onInvite = { usernameOrEmail, relationName, permissions ->
                    isInviting = true
                    inviteError = null
                    viewModel.inviteHandlerToPet(
                        UUID.fromString(petId),
                        usernameOrEmail,
                        relationName,
                        permissions
                    ) { success, error ->
                        isInviting = false
                        if (success) {
                            showAddHandlerDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Handler invited successfully!")
                            }
                        } else {
                            inviteError = error ?: "Unknown error"
                        }
                    }
                },
                isLoading = isInviting,
                errorMessage = inviteError
            )
        }
    }
}