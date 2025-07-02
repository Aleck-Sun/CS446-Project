package com.example.cs446.ui.pages.main.pets

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs446.backend.data.model.Handler
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.UserPetRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.ui.components.HandlerCard
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.view.pets.HandlerViewModel
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PermissionsScreen(
    petId: String,
    onNavigate: (MainActivityDestination, String?) -> Unit,
    viewModel: HandlerViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val petRepository = remember { PetRepository() }
    val userRepository = remember { UserRepository() }

    var pet by remember { mutableStateOf<Pet?>(null) }
//    var handlers by remember { mutableStateOf<List<Handler>>(emptyList()) }
    val handlers by viewModel.handlers.collectAsState()
    var loggedInUserId by remember { mutableStateOf<UUID?>(null) }
    val currentHandler = handlers.find { it.userId == loggedInUserId }
    val canAddHandlers by remember(currentHandler) {
        derivedStateOf { currentHandler?.permissions?.inviteHandlers ?: false }
    }

    LaunchedEffect(petId) {
        pet = petRepository.getPet(UUID.fromString(petId))
        loggedInUserId = userRepository.getCurrentUserId()
//        handlers = userPetRepository.getHandlersForPet(UUID.fromString(petId))
        viewModel.loadHandlers(UUID.fromString(petId))

    }

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
                    text = "${pet?.name ?: "My Pet"}'s Family",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(handlers) { handler ->
                    HandlerCard(
                        handler = handler,
                        onPermissionChange = { perm, value ->
                            viewModel.updatePermission(handler.name, perm, value, UUID.fromString(petId))
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}